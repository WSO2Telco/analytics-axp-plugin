package com.wso2telco.util;

public class Properties {

    private Properties() {
        throw new IllegalStateException("Utility class");
    }

    /* Kafka consumer related */
    public static final String HEALTH_CHECK_ACTIVE = "healthcheck.active";
    public static final String CONSUMER_HEALTH_CHECK_FRESHNESS_THRESHOLD = "consumer.healthCheck.freshness.time";
    // TODO error.count.freshness.threshold
    public static final String VARIABLE_FRESHNESS_THRESHOLD = "variable.freshness.time";
    public static final String RUN_TIME_KAFKA_FRESHNESS_THRESHOLD = "runtimekafka.freshness.time";
    public static final String HEALTH_CHECK_CONSUMER_ID = "healthCheck.consumer.id";
    public static final String HEALTH_CHECK_HOST = "healthCheck.host";
    public static final String HEALTH_CHECK_PORT = "healthCheck.port";

    /* Kafka producer related */
    public static final String KAFKA_ACTIVE = "kafka.active";
    public static final String KAFKA_HOST = "kafka.host";
    public static final String KAFKA_PORT = "kafka.port"; //int
    public static final String RETRIES_CONFIG = "retries"; //int
    public static final String TRANSACTION_TIMEOUT_CONFIG = "transaction.timeout"; //long
    public static final String KAFKA_TOPIC = "kafka.topic";
    public static final String MAX_THREAD_COUNT = "max.tread.count"; // int
    public static final String MAX_BLOCK_MS = "max.block.ms"; //long

}