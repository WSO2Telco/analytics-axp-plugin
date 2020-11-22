package com.wso2telco.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;

public class KafkaProducer {

    //init params
    final String KAFKA_HOST = "127.0.0.1:9092";


    public org.apache.kafka.clients.producer.KafkaProducer<String, String> createKafkaProducer() {
        //Create Producer Properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_HOST);
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "all");
        properties.setProperty(ProducerConfig.RETRIES_CONFIG, "5");
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // For an idempotent producer
        //kafka can detect whether it's a duplicate data based on the producer request id.

        //Create high throughput Producer at the expense of latency & CPU
        //properties.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        //properties.setProperty(ProducerConfig.LINGER_MS_CONFIG, "60");
        properties.setProperty(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(32 * 1024)); //32KB batch size

        //Create Kafka Producer
        org.apache.kafka.clients.producer.KafkaProducer<String, String> logProducer = new org.apache.kafka.clients.producer.KafkaProducer<String, String>(properties);
        return logProducer;
    }






}
