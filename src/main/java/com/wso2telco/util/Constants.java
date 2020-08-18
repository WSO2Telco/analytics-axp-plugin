package com.wso2telco.util;

public class Constants {
    // Properties (user configurable)
    public static final String FILE_NAME = Configurations.getInstance().getProperty(Properties.FILE_NAME );
    public static final String LOGMESSAGEDELIMITER = Configurations.getInstance().getProperty(Properties.LOGMESSAGEDELIMITER );
    public static final String LOGDATADELIMITER = Configurations.getInstance().getProperty(Properties.LOGDATADELIMITER );
    public static final String AM_MAPPING_ID = Configurations.getInstance().getProperty(Properties.AM_MAPPING_ID );

    // Constants (not user configurable)
    public static final String PATH_CONFFILE = "/config.properties";
}