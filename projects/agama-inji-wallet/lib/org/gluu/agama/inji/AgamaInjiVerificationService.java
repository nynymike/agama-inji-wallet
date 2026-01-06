package org.gluu.agama.inji;

import java.util.HashMap;
import java.util.Map;

import org.gluu.agama.inji.AgamaInjiVerificationServiceImpl;

public abstract class AgamaInjiVerificationService{

    public abstract Map<String, Object> createVpVerificationRequest();

    public abstract String createOpenidRequestUrl(String requestId, String transactionId);

    public abstract Map<String, Object> verifyInjiAppResult(Map<String, String> resultFromapp, String requestId, String transactionId);

    public static AgamaInjiVerificationService getInstance(HashMap config){
        return AgamaInjiVerificationServiceImpl.getInstance(config);
    }
}
