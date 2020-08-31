package com.wso2telco.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public final class CommonConstant {
    /* properties use in PropertyLogHandler */
    public static final String REGISTRY_PATH = "gov:/apimgt/";
    public static final String MESSAGE_TYPE = "message.type";
    public static final String PAYLOAD_LOGGING_ENABLED = "payload.logging.enabled";
    public static final String REQUEST = "request";
    public static final String RESPONSE = "response";
    public static final String REQUEST_IN = "request_in";
    public static final String REQUEST_OUT = "request_out";
    public static final String RESPONSE_IN = "response_in";
    public static final String RESPONSE_OUT = "response_out";
    public static final String ERROR_RESPONSE = "errorResponse";
    public static final String MESSAGE_ID = "MESSAGE_ID";
    public static final String ERROR = "error";
    public static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    public static final String API_RESOURCE_CACHE_KEY = "API_RESOURCE_CACHE_KEY";
    public static final String CONTENT_TYPE = "messageType";
    public static final Log REQUEST_RESPONSE_LOGGER = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");
    public static final String MC = "MC";
    public static final String AX = "AX";
    public static final String TH = "TH";
    public static final String APPLICATION_JSON = "application/json";
    public static final String TEXT_PLAIN = "text/plain";


    /* Properties use in SynapsLogHandler */
    public static final Log AXP_ANALYTICS_LOGGER = LogFactory.getLog("AXP_ANALYTICS_LOGGER");
    public static final String TRACKING_ID = "RequestId";
    //Key value to hold "to" address of the service.
    public static final String TRACKING_TO = "To";
    // Key value to hold API name with version
    public static final String TRACKING_API = "api.ut.api_version";
    // Key value to hold HTTP method
    public static final String TRACKING_HTTP_METHOD = "api.ut.HTTP_METHOD";
    // Key value to hold "to" address of the service.
    public static final String TRACKING_MESSAGE_ID = "REQUEST_ID";
    public static final String HTTP_METHOD = "HTTP_METHOD";
    public static final String ERRORINLOGGING = "Unable to set log context due to :";
    public static final String ENABLED = "enabled";

    private CommonConstant() {
        throw new IllegalStateException("Utility class");
    }

}
