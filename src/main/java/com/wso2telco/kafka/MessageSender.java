package com.wso2telco.kafka;

import com.wso2telco.scheduler.ScheduleTimerTask;
import com.wso2telco.util.Properties;
import com.wso2telco.util.PropertyReader;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import static com.wso2telco.util.CommonConstant.AXP_ANALYTICS_LOGGER;

public class MessageSender {

    ExecutorService executorService = null;
    public MessageSender(ExecutorService executorService) {
        this.executorService = executorService;
    }


    public void sendMessage(String transactionLog) {
        Runnable worker = new KafkaThreadCreator(transactionLog);
        executorService.execute(worker);
    }

    public static class KafkaThreadCreator implements Runnable {

        private final String transactionLog;
        KafkaThreadCreator(String transactionLog) {
            this.transactionLog = transactionLog;
        }

        private static String getHostname() {
            try {
                return InetAddress.getLocalHost().getHostName();
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        public void run() {
            if (Boolean.parseBoolean(PropertyReader.getKafkaProperties().get(Properties.KAFKA_ACTIVE))
                    && PropertyReader.isRuntimeKafkaEnabled()) {
                com.wso2telco.kafka.KafkaProducer kafkaProducer = new com.wso2telco.kafka.KafkaProducer();
                Producer<String, String> producer = kafkaProducer.createKafkaProducer();
                int sendMessageCount = 1;
                String transactionLogMsg = transactionLog + ",HOSTNAME:"+ getHostname().toLowerCase();

                long time = System.currentTimeMillis();

                try {
                    for (long index = time; index < time + sendMessageCount; index++) {
                        final ProducerRecord<String, String> record =
                                new ProducerRecord<String, String>(PropertyReader.getKafkaProperties().
                                        get(Properties.KAFKA_TOPIC), String.valueOf(index),
                                        transactionLogMsg);
                        producer.send(record, new Callback() {
                            public void onCompletion(RecordMetadata metadata, Exception e) {
                                if (e != null) {
                                    AXP_ANALYTICS_LOGGER.info(transactionLogMsg.replaceAll(",BODY:(.*):BODY", ""));
                                    PropertyReader.getErrorCount().setVariable(PropertyReader.getErrorCount().getVariable() + 1);
                                }
                            }
                        });
                    }
                    if ((PropertyReader.getErrorCount().getVariable() > 5) && PropertyReader.isRuntimeKafkaEnabled()) {
                        ScheduleTimerTask.runTimerDisableRuntimeKafka();
                    }
                } finally {
                    producer.flush();
                    producer.close();
                }
            } else {
                AXP_ANALYTICS_LOGGER.info(transactionLog.replaceAll(",BODY:(.*):BODY", ""));

            }
        }
    }
}
