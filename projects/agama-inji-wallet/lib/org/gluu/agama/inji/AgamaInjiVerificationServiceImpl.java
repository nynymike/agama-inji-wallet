package org.gluu.agama.inji;

import io.jans.as.common.util.CommonUtils;
import io.jans.as.common.model.registration.Client;

import io.jans.as.common.model.session.SessionId;
import io.jans.as.server.service.SessionIdService;
import jakarta.servlet.http.HttpServletRequest;
import io.jans.service.net.NetworkService;
import io.jans.service.cdi.util.CdiUtil;
import io.jans.agama.engine.script.LogUtils;
import io.jans.util.StringHelper;

import io.jans.as.model.config.WebKeysConfiguration;
import io.jans.as.model.configuration.AppConfiguration;
import io.jans.as.model.crypto.CryptoProviderFactory;
import io.jans.as.model.crypto.AbstractCryptoProvider;
import io.jans.as.model.crypto.signature.SignatureAlgorithm;

import io.jans.as.model.exception.CryptoProviderException;
import io.jans.as.model.exception.InvalidJwtException;

import io.jans.as.model.jwk.JSONWebKey;
import io.jans.as.model.jwk.Use;
import io.jans.as.model.jwt.Jwt;
import io.jans.as.model.jwt.JwtHeader;
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

    private String INJI_API_ENDPOINT = "http://mmrraju-comic-pup.gluu.info/backend/consent/new"; // Actual INJI Backend URL
    private String INJI_BACKEND_BASE_URL = "http://mmrraju-comic-pup.gluu.info";
    private String INJI_RFAC_BASE_URL = "";
    private String  CLIENT_ID;
    public static String CALLBACK_URL= "https://mmrraju-promoted-macaque.gluu.info/jans-auth/fl/callback"; // Agama call-back URL
    private String RFAC_DEMO_BASE = "https://mmrraju-adapted-crab.gluu.info/inji-user.html"; // INJI RP URL.

    private static AgamaInjiVerificationServiceImpl INSTANCE = null;


    public AgamaInjiVerificationServiceImpl(){}

    public static synchronized AgamaInjiVerificationServiceImpl getInstance()
    {
        
        if (INSTANCE == null)
            INSTANCE = new AgamaInjiVerificationServiceImpl();
        return INSTANCE;
    } 

    @Override
    public Map<String, Object> verifyServiceURL() {

        Map<String, Object> responseMap = new HashMap<>();

        try {
            LogUtils.log("Retrieve  session...");
            Map<String, String> sessionAttrs = getSessionId().getSessionAttributes();
            
            LogUtils.log(sessionAttrs);
            String clientId = sessionAttrs.get("client_id");
            this.CLIENT_ID = clientId;
            // Build DEMO Authorization Request Payload
            LogUtils.log("Build authorization request Payload and send a POST Request to INJI BACKEND API...");
            Map<String, Object> authRequest = new HashMap<>();
            // authRequest.put("client_id", clientId);
            // authRequest.put("scope", "openid");
            // authRequest.put("response_type", "vp_token");
            // authRequest.put("nonce", UUID.randomUUID().toString());
            // authRequest.put("state", UUID.randomUUID().toString());

            authRequest.put("consent_id", "intent-id-123456");
            authRequest.put("status", "Authorised");
            authRequest.put("permissions", List.of(
                                "ReadAccountsBasic",
                                "ReadBalances"
                        ));
            authRequest.put("expires_in", 3600);

            String jsonPayload = new ObjectMapper().writeValueAsString(authRequest);

            // String endpoint = this.INJI_BACKEND_BASE_URL + "/verifyServiceURL/vp-request";
            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.NORMAL) 
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(INJI_API_ENDPOINT))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Cache-Control", "no-cache")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                LogUtils.log("ERROR: INJI Verify returned status code: %", response.statusCode());
                responseMap.put("valid", false);

                responseMap.put("message", "ERROR: INJI BACKEND returned status code: % "+response.statusCode());
                return responseMap;
            }
            String jsonResponse = response.body();
            LogUtils.log("INJI Backend Response: %", jsonResponse);

            // Parse JSON using your existing ObjectMapper
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> data = mapper.readValue(jsonResponse, Map.class);
            // Map<String, Object> data = (Map<String, Object>) consentMap.get("Data");

            // if (data == null || !data.containsKey("requestId") || !data.containsKey("transactionId")) {
            //     LogUtils.log("ERROR: Missing Data from INJI backend Response response");
            //     responseMap.put("valid", false);
            //     responseMap.put("message", "ERROR: Missing Data from INJI backend response");
            // }  
            if (data == null || !data.containsKey("Status")) {
                LogUtils.log("ERROR: Missing Data from INJI backendresponse");
                responseMap.put("valid", false);
                responseMap.put("message", "ERROR: Missing Data from INJI backend response");
                return responseMap;
            }   
            
            String status = (String) data.get("Status");
            LogUtils.log("INJI Backend status: %", status);
            if("Authorised".equalsIgnoreCase(status)){
                responseMap.put("valid", true);
                responseMap.put("message", "INJI Backed System response is satisfy");
                responseMap.put("requestId", "R_ID_12345");
                responseMap.put("transactionId", "TXN_ID_12345");
                return responseMap;               
            }

        } catch (Exception e) {
            responseMap.put("valid", false);
            responseMap.put("message", e.getMessage());
        }

        return responseMap;
    }


    @Override
    public String createOpenidRequestUrl(String requestId, String transactionId) {
       try {
            LogUtils.log("Preparing OpenID4VP Request");
            // Generate state + nonce
            String state = UUID.randomUUID().toString();
            String nonce = generateNonce(24);

            // Build minimal OpenID4VP request object
            JSONObject openidRequest = new JSONObject();
            openidRequest.put("client_id", this.CLIENT_ID);
            openidRequest.put("response_type", "id_token");
            openidRequest.put("scope", "openid");
            openidRequest.put("callback_uri", CALLBACK_URL);
            openidRequest.put("state", state);
            openidRequest.put("nonce", nonce);

            // Correlation to the previously created request
            openidRequest.put("request_id", requestId);
            openidRequest.put("transaction_id", transactionId);

            // Minimal presentation_definition placeholder (replace with real PD later)
            Map<String, Object> pd = new HashMap<>();
            pd.put("id", "pd-" + UUID.randomUUID());
            pd.put("input_descriptors", new Object[] {
                    Map.of(
                        "id", "placeholder-cred",
                        "purpose", "Placeholder: replace with real presentation definition when ready"
                    )
            });
            openidRequest.put("presentation_definition", pd);

            String encodedPayload = Base64.getUrlEncoder().withoutPadding()
                                .encodeToString(openidRequest.toString().getBytes(StandardCharsets.UTF_8));
            String url = RFAC_DEMO_BASE + "?request=" + URLEncoder.encode(encodedPayload, StandardCharsets.UTF_8);

            // Optionally include state and requestId explicitly as query params for clarity:
            url += "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
            url += "&requestId=" + URLEncoder.encode(requestId, StandardCharsets.UTF_8);

            // Return the ready-to-open URL
            return url;

        } catch (Exception e) {
            // handle/log error depending on your logging utils
            LogUtils.log("ERROR: Failed to build OpenID request URL: %", e.getMessage());
            return null;
        }
    }

    @Override
    public Map<String, Object> verifyInjiAppResult(Map<String, String> resultFromApp, String requestId, String transactionId) {
        // Example implementation
        Map<String, Object> response = new HashMap<>();
        // if (resultFromApp.containsKey("status") && "success".equals(resultFromApp.get("status"))) {
        //     response.put("verified", true);
        //     response.put("message", "Verification successful");
        // } else {
        //     response.put("verified", false);
        //     response.put("message", "Verification failed");
        // }

        LogUtils.log("INJI user back to agama...");

        LogUtils.log("Data : %", resultFromApp);
        String requestIdStatus = checkRequestIdStatus(requestId);

        if (!"VP_SUBMITTED".equals(requestIdStatus)) {
            response.put("valid", false);
            response.put("message", "Error: Request id is EXPIRED");
            return response;
        }

        String transactionIdStatus = checkTransactionIdStatus(transactionId);

        if (!"VALID".equals(transactionIdStatus)) {
            response.put("valid", false);
            response.put("message", "Error: Transaction id is INVALID");
            return response;
        }

        response.put("valid", true);
        response.put("message", "Verification successful");
        return response;

    }
    
    private String checkTransactionIdStatus(String transactionId) {
        try {
            LogUtils.log("Validating INJI Transaction-id for : %", transactionId);
            // String apiUrl = this.INJI_BACKEND_BASE_URL + "/vp-result/" + transactionId;
            String apiUrl = "http://mmrraju-comic-pup.gluu.info/account-access-consents/" + "intent-id-123456";

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

            if (response.statusCode() != 200) {
                LogUtils.log("ERROR: INJI BACKEND API returned status code: %", response.statusCode());
                return "UNKNOWN";
            }

            LogUtils.log("INJI VERIFY BACKEND RESPONSE FOR TRANSACTION-ID : %", response.body());
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> mapData = mapper.readValue(response.body(), Map.class);
            Map<String, Object> data = (Map<String, Object>) mapData.get("Data");

            if (data != null || data.containsKey("Status")) {
                // return data.get("status").toString();
                return "VALID";
            } else {
                return "UNKNOWN";
            }

        } catch (Exception e) {
            LogUtils.log("ERROR: Exception in checkTransactionIdStatus: %", e.getMessage());
            return "UNKNOWN";
        }
    }

    private String checkRequestIdStatus(String requestId) {
        try {

            LogUtils.log("Validating INJI Request-id for : %", requestId);
            // String apiUrl = this.INJI_BACKEND_BASE_URL + "/verifyServiceURL/vp-request/" + requestId + "/status";

            String apiUrl = "http://mmrraju-comic-pup.gluu.info/account-access-consents/" + "intent-id-123456";

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

            if (response.statusCode() != 200) {
                LogUtils.log("ERROR: INJI BACKEND API returned status code: %", response.statusCode());
                return "UNKNOWN";
            }

            LogUtils.log("INJI VERIFY BACKEND RESPONSE FOR REQUEST-ID : %", response.body());
            //  Parse JSON response
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> mapData = mapper.readValue(response.body(), Map.class);
            Map<String, Object> data = (Map<String, Object>) mapData.get("Data");

            if (data != null || data.containsKey("Status")) {
                // return data.get("status").toString();
                return "VP_SUBMITTED";
            } else {
                return "UNKNOWN";
            }

        } catch (Exception e) {
            LogUtils.log("ERROR: Exception in checkRequestIdStatus: %", e.getMessage());
            return "UNKNOWN";
        }
    }

    private SessionId getSessionId() {
        SessionIdService sis = CdiUtil.bean(SessionIdService.class); 
        return sis.getSessionId(CdiUtil.bean(HttpServletRequest.class));
    }   
    
    private static String generateNonce(int length) {
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }    

}