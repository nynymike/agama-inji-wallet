package org.gluu.agama.inji;

import io.jans.orm.exception.operation.EntryNotFoundException;
import io.jans.as.common.model.common.User;
import io.jans.as.common.service.common.UserService;
import io.jans.as.common.util.CommonUtils;
import io.jans.as.common.model.registration.Client;

import io.jans.as.common.model.session.SessionId;
import io.jans.as.server.service.SessionIdService;
import jakarta.servlet.http.HttpServletRequest;
import io.jans.service.net.NetworkService;
import io.jans.service.cdi.util.CdiUtil;
import io.jans.agama.engine.script.LogUtils;
import io.jans.util.StringHelper;

// import io.jans.as.model.config.WebKeysConfiguration;
// import io.jans.as.model.configuration.AppConfiguration;
// import io.jans.as.model.crypto.CryptoProviderFactory;
// import io.jans.as.model.crypto.AbstractCryptoProvider;
// import io.jans.as.model.crypto.signature.SignatureAlgorithm;

// import io.jans.as.model.exception.CryptoProviderException;
import io.jans.as.model.exception.InvalidJwtException;

// import io.jans.as.model.jwk.JSONWebKey;
// import io.jans.as.model.jwk.Use;
// import io.jans.as.model.jwt.Jwt;
// import io.jans.as.model.jwt.JwtHeader;
import io.jans.service.cdi.util.CdiUtil;
import io.jans.as.server.service.ClientService;

import io.jans.util.security.StringEncrypter.EncryptionException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.io.*;

// import com.nimbusds.jose.*;
// import com.nimbusds.jose.crypto.RSASSASigner;
// import com.nimbusds.jose.jwk.RSAKey;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.gluu.agama.inji.AgamaInjiVerificationService;

public class AgamaInjiVerificationServiceImpl extends AgamaInjiVerificationService{

    //
    private String APP_USER_MAIL;
    private String APP_USER_NAME;
    //
    private static final String INUM_ATTR = "inum";
    private static final String UID = "uid";
    private static final String MAIL = "mail";
    private static final String CN = "cn";
    private static final String DISPLAY_NAME = "displayName";
    private static final String GIVEN_NAME = "givenName";
    private static final String SN = "sn";
    private String USER_INFO_FROM_VC = null;
    // private String INJI_API_ENDPOINT = "http://mmrraju-comic-pup.gluu.info/backend/consent/new";
    private String INJI_BACKEND_BASE_URL = "https://injiverify.collab.mosip.net";
    private String INJI_WEB_BASE_URL = "https://injiweb.collab.mosip.net";
    private String  CLIENT_ID = "agama-app";
    private Map<String, Object> AUTHORIZATION_DETAILS = new HashMap<>();
    private String NONCE ;
    private String RESPONSE_URL ;

    public  String CALLBACK_URL= ""; // Agama call-back URL
    private String RFAC_DEMO_BASE = "https://mmrraju-adapted-crab.gluu.info/inji-user.html"; // INJI RP URL.
    private HashMap<String, Object> flowConfig;
    private HashMap<String, Object> PRESENATION_DEFINITION;
    private HashMap<String, Object> CLIENT_METADATA;
    private HashMap<String, String> VC_TO_GLUU_MAPPING; 
    private static AgamaInjiVerificationServiceImpl INSTANCE = null;

    public AgamaInjiVerificationServiceImpl(){}

    public AgamaInjiVerificationServiceImpl(HashMap config){
        if(config != null){
            LogUtils.log("Flow config provided is: %", config);
            flowConfig = config;

            this.INJI_BACKEND_BASE_URL = flowConfig.get("injiVerifyBaseURL") !=null ? flowConfig.get("injiVerifyBaseURL").toString() : INJI_BACKEND_BASE_URL;
            this.INJI_WEB_BASE_URL = flowConfig.get("injiWebBaseURL") !=null ? flowConfig.get("injiWebBaseURL").toString() : INJI_WEB_BASE_URL;
            this.CLIENT_ID = flowConfig.get("clientId") != null ? flowConfig.get("clientId").toString() : CLIENT_ID;
            this.PRESENATION_DEFINITION = flowConfig.get("presentationDefinition") !=null ? (HashMap<String, Object>) flowConfig.get("presentationDefinition") : this.getPresentationDefinitionSample();
            this.CLIENT_METADATA = flowConfig.get("clientMetadata")  !=null ? (HashMap<String, Object>) flowConfig.get("clientMetadata") : this.buildClientMetadata();
            this.CALLBACK_URL = flowConfig.get("agamaCallBackUrl") != null ? flowConfig.get("agamaCallBackUrl").toString() : CALLBACK_URL;
            this.VC_TO_GLUU_MAPPING = flowConfig.get("vcToGluuMapping") !=null ? (HashMap<String, String>) flowConfig.get("vcToGluuMapping"): VC_TO_GLUU_MAPPING;
        }else{
            LogUtils.log("Error: No configuration provided using default may not work properly...");
        }


    }

    public static synchronized AgamaInjiVerificationServiceImpl getInstance(HashMap config)
    {
        
        if (INSTANCE == null)
            INSTANCE = new AgamaInjiVerificationServiceImpl(config);
        return INSTANCE;
    } 

    @Override
    public Map<String, Object> createVpVerificationRequest() {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            // LogUtils.log("Retrieve  session...");
            Map<String, String> sessionAttrs = getSessionId().getSessionAttributes();

            LogUtils.log(sessionAttrs);
            String clientId = sessionAttrs.get("client_id");
            this.CLIENT_ID = clientId;
            LogUtils.log("Create VP Verification Request...");
            Map<String, Object> requestPayload = new HashMap<>();
            requestPayload.put("clientId", CLIENT_ID);
            requestPayload.put("presentationDefinition", PRESENATION_DEFINITION);
            String jsonPayload = new ObjectMapper().writeValueAsString(requestPayload);
            LogUtils.log("Payload object: %", requestPayload);
            LogUtils.log("Payload JSON: %", jsonPayload);
            String endpoint = this.INJI_BACKEND_BASE_URL + "/v1/verify/vp-request";

            
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL) 
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Cache-Control", "no-cache")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String jsonResponse = response.body();
                LogUtils.log("INJI Verify Backend Response: %", jsonResponse);
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = mapper.readValue(jsonResponse, Map.class);

                if (data == null || !data.containsKey("requestId") || !data.containsKey("transactionId")) {
                    LogUtils.log("ERROR: Missing Data from INJI backend Response response");
                    responseMap.put("valid", false);
                    responseMap.put("message", "ERROR: Missing Data from INJI Verify backend response");
                }  
                String transactionId = (String) data.get("transactionId");
                String requestId = (String) data.get("requestId");
                this.AUTHORIZATION_DETAILS = (Map<String, Object>) data.get("authorizationDetails");
                LogUtils.log("Authorization details : %", this.AUTHORIZATION_DETAILS);
                responseMap.put("valid", true);
                responseMap.put("message", "INJI Verify Backed System response is satisfy");
                responseMap.put("requestId", requestId);
                responseMap.put("transactionId", transactionId);
                return responseMap;               
           
            }else{
                LogUtils.log("ERROR: INJI Verify returned status code: %", response.statusCode());
                responseMap.put("valid", false);

                responseMap.put("message", "ERROR: INJI BACKEND returned status code: % "+response.statusCode());
                return responseMap;
            }


        } catch (Exception e) {
            responseMap.put("valid", false);
            responseMap.put("message", e.getMessage());
        }

        return responseMap;
    }


    @Override
    public String buildInjiWebAuthorizationUrl(String requestId, String transactionId) {
        try {
            LogUtils.log("Preparing Inji web Authorization Url...");

            String nonce = this.AUTHORIZATION_DETAILS.get("nonce").toString();
            // LogUtils.log("NONCE : %", nonce);
            String baseUrl = this.INJI_WEB_BASE_URL + "/authorize";

            String presentationDefinitionJson = new JSONObject(this.AUTHORIZATION_DETAILS.get("presentationDefinition")).toString();
            // LogUtils.log("Presentation defenation: %", presentationDefinitionJson);
            String clientMetadataJson = new JSONObject(this.CLIENT_METADATA).toString();

            // LogUtils.log(clientMetadataJson);
            String url = baseUrl +
                    "?client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8) +
                    "&presentation_definition=" + URLEncoder.encode(presentationDefinitionJson, StandardCharsets.UTF_8) +
                    "&nonce=" + URLEncoder.encode(nonce, StandardCharsets.UTF_8) +
                    "&response_uri=" + URLEncoder.encode((String) this.AUTHORIZATION_DETAILS.get("responseUri"), StandardCharsets.UTF_8) +
                    "&redirect_uri=" + URLEncoder.encode(this.CALLBACK_URL, StandardCharsets.UTF_8) +
                    "&response_type=" +this.AUTHORIZATION_DETAILS.get("responseType")  +
                    "&response_mode=" + this.AUTHORIZATION_DETAILS.get("responseMode") +
                    "&client_id_scheme=pre-registered" +
                    "&state=" + URLEncoder.encode(requestId, StandardCharsets.UTF_8) +
                    "&client_metadata=" + URLEncoder.encode(clientMetadataJson, StandardCharsets.UTF_8);

            LogUtils.log("URL : %", url);
            return url;
            // return RFAC_DEMO_BASE;

        } catch (Exception e) {
            LogUtils.log("ERROR: Failed to build Inji Web Authorization URL: %", e.getMessage());
            return null;
        }
    }


    @Override
    public Map<String, Object> verifyInjiAppResult(String requestId, String transactionId) {
        Map<String, Object> response = new HashMap<>();

        LogUtils.log("INJI user back to agama...");

        LogUtils.log("Data : requestId : % transactionId : %", requestId, transactionId);

        // APP_USER_MAIL = resultFromApp.get("email");
        // APP_USER_NAME = resultFromApp.get("name");

        String requestIdStatus = checkRequestIdStatus(requestId);

        if (!"VP_SUBMITTED".equals(requestIdStatus)) {
            response.put("valid", false);
            response.put("message", "Error: VP REQUEST ID STATUS is " + requestIdStatus);
            return response;
        }

        String transactionIdStatus = checkTransactionIdStatus(transactionId);

        if (!"SUCCESS".equals(transactionIdStatus)) {
            response.put("valid", false);
            response.put("message", "Error: No VP submission found for given transaction ID " + transactionIdStatus);
            return response;
        }

        response.put("valid", true);
        response.put("message", "VP TOKEN Verification successful");
        return response;

    }    
    
    private String checkTransactionIdStatus(String transactionId) {
        try {
            LogUtils.log("Validating VP TRANSACTION ID STATUS for : %", transactionId);
            String apiUrl = this.INJI_BACKEND_BASE_URL + "/v1/verify/vp-result/" + transactionId;
            // String apiUrl = "http://mmrraju-comic-pup.gluu.info/account-access-consents/" + "intent-id-123456";

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                LogUtils.log("INJI VERIFY BACKEND RESPONSE FOR TRANSACTION-ID : %", response.body());
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = mapper.readValue(response.body(), Map.class);
                // Map<String, Object> data = (Map<String, Object>) mapData.get("Data");

                if (data != null || data.containsKey("vpResultStatus")) {
                    List<Map<String, Object>> vcResults = (List<Map<String, Object>>) data.get("vcResults");
                    String vc = (String) vcResults.get(0).get("vc");
                    this.USER_INFO_FROM_VC = vc;
                    return data.get("vpResultStatus").toString();
                } else {
                    return "UNKNOWN";
                }
            }else{
                LogUtils.log("ERROR: INJI VP TOKEN FOR TRANSACTION ID status code: %", response.statusCode());
                return "UNKNOWN";
            }

            

        } catch (Exception e) {
            LogUtils.log("ERROR: Exception in checkTransactionIdStatus: %", e.getMessage());
            return "UNKNOWN";
        }
    }

    private String checkRequestIdStatus(String requestId) {
        try {

            LogUtils.log("Validating VP REQUEST STATUS for : %", requestId);
            String apiUrl = this.INJI_BACKEND_BASE_URL + "/v1/verify/vp-request/" + requestId + "/status";
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Cache-Control", "no-cache")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode()== 200) {
                LogUtils.log("INJI VERIFY BACKEND RESPONSE FOR REQUEST-ID : %", response.body());
                ObjectMapper mapper = new ObjectMapper();
                Map<String, Object> data = mapper.readValue(response.body(), Map.class);

                if (data != null || data.containsKey("status")) {
                    LogUtils.log("VP REQUEST STATUS : %", data.get("status") );
                    return data.get("status").toString();
                } else {
                    return "UNKNOWN";
                }
            }else{
                LogUtils.log("ERROR: VP Request status code: %", response.statusCode());
                return "UNKNOWN";
            }
        } catch (Exception e) {
            LogUtils.log("ERROR: Exception in GET VP Request STATUS: %", e.getMessage());
            return "UNKNOWN";
        }
    }

    private SessionId getSessionId() {
        SessionIdService sis = CdiUtil.bean(SessionIdService.class); 
        return sis.getSessionId(CdiUtil.bean(HttpServletRequest.class));
    }   
    
    // private static String generateNonce(int length) {
    //     SecureRandom rnd = new SecureRandom();
    //     StringBuilder sb = new StringBuilder(length);
    //     String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    //     for (int i = 0; i < length; i++) {
    //         sb.append(chars.charAt(rnd.nextInt(chars.length())));
    //     }
    //     return sb.toString();
    // }    

    private HashMap<String, Object> getPresentationDefinitionSample(){

            Map<String, Object> presentationDefinition = new HashMap<>();
            presentationDefinition.put("id", "c4822b58-7fb4-454e-b827-f8758fe27f9a");
            presentationDefinition.put(
                    "purpose",
                    "Relying party is requesting your digital ID for the purpose of Self-Authentication"
            );

            presentationDefinition.put(
                    "format",
                    Map.of(
                            "ldp_vc",
                            Map.of("proof_type", new String[]{"Ed25519Signature2020"})
                    )
            );

            presentationDefinition.put(
                    "input_descriptors",
                    new Object[]{
                            Map.of(
                                    "id", "id card credential",
                                    "format", Map.of(
                                            "ldp_vc",
                                            Map.of("proof_type", new String[]{"RsaSignature2018"})
                                    ),
                                    "constraints", Map.of(
                                            "fields", new Object[]{
                                                    Map.of(
                                                            "path", List.of('$.type'),
                                                            "filter", Map.of(
                                                                    "type", "object",
                                                                    "pattern", "MOSIPVerifiableCredential"
                                                            )
                                                    )
                                            }
                                    )
                            )
                    }
            );   
            return presentationDefinition;     
    }

    private HashMap<String, Object> buildClientMetadata() {

        HashMap<String, Object> clientMetadata = new HashMap<>();

        clientMetadata.put("client_name", "Agama Application");
        clientMetadata.put("logo_uri",
                "https://mosip.github.io/inji-config/logos/StayProtectedInsurance.png");

        HashMap<String, Object> ldpVp = new HashMap<>();
        ldpVp.put("proof_type", List.of(
                "Ed25519Signature2018",
                "Ed25519Signature2020",
                "RsaSignature2018"
        ));

        HashMap<String, Object> vpFormats = new HashMap<>();
        vpFormats.put("ldp_vp", ldpVp);

        clientMetadata.put("vp_formats", vpFormats);

        return clientMetadata;
    }

    // This method will use for our demo.
    private Map<String, String> addAsNewUser(String email, String displayName){
        User user = getUser(MAIL, email);
        boolean local = user != null;
        LogUtils.log("There is % local account for %", local ? "a" : "no", email);
        if (local) {
            String uid = getSingleValuedAttr(user, UID);
            String inum = getSingleValuedAttr(user, INUM_ATTR);
            String name = getSingleValuedAttr(user, GIVEN_NAME);

            if (name == null) {
                name = getSingleValuedAttr(user, DISPLAY_NAME);

                if (name == null) {
                    name = email.substring(0, email.indexOf("@"));
                    }
            }

            return new HashMap<>(Map.of(UID, uid, INUM_ATTR, inum, "email", email));
        }else{
            User newUser = new User();
            String uid = email.substring(0, email.indexOf("@"));
        
            newUser.setAttribute(UID, uid);
            newUser.setAttribute(MAIL, email);
            newUser.setAttribute(DISPLAY_NAME, displayName);

            UserService userService = CdiUtil.bean(UserService.class);
            newUser = userService.addUser(newUser, true);
            if (newUser == null){
                LogUtils.log("Added user not found");
                return null;
            };
            LogUtils.log("New user added : %", email);
            String inum = getSingleValuedAttr(newUser, INUM_ATTR);
            return new HashMap<>(Map.of(UID, uid, INUM_ATTR, inum, "email", email));
                
        } 
    }

    @Override
    public Map<String, String> onboardUser() {
        ObjectMapper mapper = new ObjectMapper();
        if(USER_INFO_FROM_VC!=null){
            Map<String, Object> vcMap = mapper.readValue(this.USER_INFO_FROM_VC, Map.class);
            Map<String, Object> credentialSubject = (Map<String, Object>) vcMap.get("credentialSubject");
            String email = (String) credentialSubject.get("email");
            User user = getUser(MAIL, email);
            boolean local = user != null;
            LogUtils.log("There is % local account for %", local ? "a" : "no", email);
            if (local) {
                String uid = getSingleValuedAttr(user, UID);
                String inum = getSingleValuedAttr(user, INUM_ATTR);
                String name = getSingleValuedAttr(user, GIVEN_NAME);

                if (name == null) {
                    name = getSingleValuedAttr(user, DISPLAY_NAME);

                    if (name == null) {
                        name = email.substring(0, email.indexOf("@"));
                    }
                }

                return new HashMap<>(Map.of(UID, uid, INUM_ATTR, inum, "name", name, "email", email));
            }else{
                User newUser = new User();
                String uid = email.substring(0, email.indexOf("@"));
                List<Map<String, Object>> fullName = (List<Map<String, Object>>) credentialSubject.get("fullName");
                String displayName = (String) fullName.get(0).get("value");

                newUser.setAttribute(UID, uid);
                newUser.setAttribute(MAIL, email);
                newUser.setAttribute(DISPLAY_NAME, displayName);

                UserService userService = CdiUtil.bean(UserService.class);
                newUser = userService.addUser(newUser, true);
                if (newUser == null){
                    LogUtils.log("Added user not found");
                    return null;
                };
                LogUtils.log("New user added : %", email);
                String inum = getSingleValuedAttr(newUser, INUM_ATTR);
                return new HashMap<>(Map.of(UID, uid, INUM_ATTR,inum, DISPLAY_NAME, displayName, "email", email));
                
            }
        }

        LogUtils.log("Error: No user info found from VC");
        return null;
        // return addAsNewUser(APP_USER_MAIL, DISPLAY_NAME);
    }

    private static User getUser(String attributeName, String value) {
        UserService userService = CdiUtil.bean(UserService.class);
        return userService.getUserByAttribute(attributeName, value, true);
    }    
    private String getSingleValuedAttr(User user, String attribute) {

        Object value = null;
        if (attribute.equals(UID)) {
            //user.getAttribute("uid", true, false) always returns null :(
            value = user.getUserId();
        } else {
            value = user.getAttribute(attribute, true, false);
        }
        return value == null ? null : value.toString();

    }
}