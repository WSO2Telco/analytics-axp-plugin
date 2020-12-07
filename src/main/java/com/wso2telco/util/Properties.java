package com.wso2telco.util;

public class Properties {
    // Properties (user configurable)
    public static final String FILE_NAME = "config.file.name";
    public static final String LOGMESSAGEDELIMITER = "log.message.delimeter";
    public static final String LOGDATADELIMITER = "log.data.delimeter";
    public static final String AM_MAPPING_ID = "am.mapping.id";
    public static final String VARIABLE_FRESHNESS_THRESHOLD = "variable.freshness.threshold";
    public static final String RUNTIMEKAFKA_FRESHNESS_THRESHOLD = "runtimekafka.freshness.threshold";

    private Properties() {
        throw new IllegalStateException("Utility class");
    }
}