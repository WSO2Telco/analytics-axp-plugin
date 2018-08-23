package com.wso2telco.logging;

import org.apache.axis2.context.MessageContext;

public class UniqueIDGenerator {

    private static long id;
    private static final String REQUEST_ID = "mife.prop.requestId";

    public static synchronized String generateAndSetUniqueID(String axtype, MessageContext context) {
        String requestId = System.currentTimeMillis()+axtype+"0"+ id++;
        context.setProperty(REQUEST_ID, requestId);
        return requestId;
    }
}
