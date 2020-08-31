package com.wso2telco.util;

import java.util.Properties;

public final class Configurations {

    private static Configurations instance = null;
    private Properties properties = null;

    /**
     * Private constructor
     */
    private Configurations() {
        this.properties = new Properties();
        try {
            properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(Constants.PATH_CONFFILE));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Creates the instance is synchronized to avoid multithreads problems
     */
    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new Configurations();
        }
    }

    /**
     * Get the properties instance. Uses singleton pattern
     */
    public static Configurations getInstance() {
        // Uses singleton pattern to guarantee the creation of only one instance
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    /**
     * Get a property of the property file
     */
    public String getProperty(String key) {
        String result = null;
        if (key != null && !key.trim().isEmpty()) {
            result = this.properties.getProperty(key);
        }
        return result;
    }

    /**
     * Override the clone method to ensure the "unique instance" requirement of this class
     */
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }
}