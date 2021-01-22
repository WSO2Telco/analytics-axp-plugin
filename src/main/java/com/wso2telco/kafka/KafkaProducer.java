package com.wso2telco.kafka;

import com.wso2telco.util.Properties;
import com.wso2telco.util.PropertyReader;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaProducer {

    private KafkaProducer() {
        throw new IllegalStateException("Utility class");
    }
    public static org.apache.kafka.clients.producer.KafkaProducer<String, String> createKafkaProducer() {
        //Create Producer Properties
        java.util.Properties properties = new java.util.Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, PropertyReader.getKafkaProperties()
                .get(Properties.KAFKA_HOST)+":"+ PropertyReader.getKafkaProperties().get(Properties.KAFKA_PORT));
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, PropertyReader.getKafkaProperties()
                .get(Properties.RETRIES_CONFIG));
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // For an idempotent producer
        properties.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, PropertyReader.getKafkaProperties()
                .get(Properties.TRANSACTION_TIMEOUT_CONFIG));
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, PropertyReader.getKafkaProperties()
                .get(Properties.MAX_BLOCK_MS));

        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(100 * 1024)); //32KB batch size

        //Create Kafka Producer
        return new org.apache.kafka.clients.producer.KafkaProducer<>(properties);
    }

}
