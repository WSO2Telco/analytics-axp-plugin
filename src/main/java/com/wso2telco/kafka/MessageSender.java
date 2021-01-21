package com.wso2telco.kafka;

import com.wso2telco.scheduler.ScheduleTimerTask;
import com.wso2telco.util.Properties;
import com.wso2telco.util.PropertyReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;

import static com.wso2telco.util.CommonConstant.AXP_ANALYTICS_LOGGER;

public class MessageSender {

    private static final Log log =  LogFactory.getLog(MessageSender.class);
    ExecutorService executorService = null;
    public MessageSender(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void sendMessage(String transactionLog) {
        Runnable worker = new KafkaDataPublisher(transactionLog);
        executorService.execute(worker);
    }

    public static class KafkaDataPublisher implements Runnable {

        private String transactionLog;
        KafkaDataPublisher(String transactionLog) {
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
                Producer<String, String> producer = com.wso2telco.kafka.KafkaProducer.createKafkaProducer();
                int sendMessageCount = 1;
                //TODO remove string calculations
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
                                    if (log.isDebugEnabled()) {
                                        log.debug("started writing to axp-analytics-logger file since "+ e.getMessage());
                                    }
                                    AXP_ANALYTICS_LOGGER.info(transactionLogMsg.replaceAll(",BODY:(.*):BODY", ""));
                                    PropertyReader.getErrorCount().setVariable(PropertyReader.getErrorCount().getVariable() + 1);
                                }
                            }
                        });
                    }
                    if (PropertyReader.getErrorCount().getVariable() > 5) {
                        ScheduleTimerTask.runTimerDisableRuntimeKafka();
                    }
                } finally {
                    producer.flush();
                    producer.close();
                    transactionLog = null;
                }
            } else {
                AXP_ANALYTICS_LOGGER.info(transactionLog.replaceAll(",BODY:(.*):BODY", ""));

            }
        }

    }
}
