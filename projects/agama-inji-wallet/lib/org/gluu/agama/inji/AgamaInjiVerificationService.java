package org.gluu.agama.inji;

import java.util.HashMap;
import java.util.Map;

import org.gluu.agama.inji.AgamaInjiVerificationServiceImpl;

public abstract class AgamaInjiVerificationService{

    public abstract Map<String, Object> verifyServiceURL();

    public abstract String createOpenidRequestUrl(String requestId, String transactionId);

    public abstract Map<String, Object> verifyInjiAppResult(Map<String, String> resultFromapp, String requestId, String transactionId);

    public static AgamaInjiVerificationService getInstance(){
        return AgamaInjiVerificationServiceImpl.getInstance();
    }
}
