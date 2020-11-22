package com.wso2telco.kafka;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageSender {
    final static String KAFKA_TOPIC = "test";
    final static int MAX_THREAD_COUNT = 2;

    public void sendMessage(String transactionLog) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
        Runnable worker = new KafkaThreadCreator(transactionLog);
        executor.execute(worker);
    }

    public static class KafkaThreadCreator implements Runnable {
        private final String transactionLog;

        KafkaThreadCreator(String transactionLog) {
            this.transactionLog = transactionLog;
        }

        @Override
        public void run() {
            com.wso2telco.kafka.KafkaProducer kafkaProducer = new com.wso2telco.kafka.KafkaProducer();
            Producer<String, String> producer = kafkaProducer.createKafkaProducer();
            int sendMessageCount = 1;

            long time = System.currentTimeMillis();

            try {
                for (long index = time; index < time + sendMessageCount; index++) {
                    final ProducerRecord<String, String> record =
                            new ProducerRecord<String, String>(KAFKA_TOPIC, String.valueOf(index),
                                    transactionLog+ index);
                    System.out.println("before       : "+transactionLog);
                    producer.send(record, new Callback() {
                        public void onCompletion(RecordMetadata metadata, Exception e) {
                            if(e != null) {
                                System.out.println("After       : "+transactionLog);
                                System.out.println("write to a log file. but need to confirm that transactionLog is having same log message");
                            } else {
                                System.out.println("The offset of the record we just sent is: " + metadata.offset());
                            }
                        }
                    });
                }

            } finally {
                producer.flush();
                producer.close();
            }
        }

    }
}
