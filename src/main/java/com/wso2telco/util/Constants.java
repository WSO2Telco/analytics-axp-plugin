package com.wso2telco.util;

public class Constants {
    // Properties (user configurable)
    public static final String FILE_NAME = Configurations.getInstance().getProperty(Properties.FILE_NAME);
    public static final String LOGMESSAGEDELIMITER = Configurations.getInstance().getProperty(Properties.LOGMESSAGEDELIMITER);
    public static final String LOGDATADELIMITER = Configurations.getInstance().getProperty(Properties.LOGDATADELIMITER);
    public static final String AM_MAPPING_ID = Configurations.getInstance().getProperty(Properties.AM_MAPPING_ID);
    public static final long VARIABLE_FRESHNESS_THRESHOLD = Long.parseLong(Configurations.getInstance().getProperty(Properties.VARIABLE_FRESHNESS_THRESHOLD));
    public static final long RUNTIMEKAFKA_FRESHNESS_THRESHOLD = Long.parseLong(Configurations.getInstance().getProperty(Properties.RUNTIMEKAFKA_FRESHNESS_THRESHOLD));
    // Constants (not user configurable)
    public static final String PATH_CONFFILE = "/config.properties";

    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}