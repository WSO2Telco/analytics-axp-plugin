package com.wso2telco.scheduler;

import com.wso2telco.util.HealthCheckHttpClient;
import com.wso2telco.util.Properties;
import com.wso2telco.util.PropertyReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Timer;
import java.util.TimerTask;

public class ScheduleTimerTask extends TimerTask {

    private static final Log log =  LogFactory.getLog(ScheduleTimerTask.class);

    public static void runTimerHealthCheck() {
        TimerTask timerTask = new ScheduleTimerTask();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, Long.parseLong(PropertyReader.getKafkaProperties().
                get(Properties.CONSUMER_HEALTH_CHECK_FRESHNESS_THRESHOLD)));
    }

    public static void runTimerDisableRuntimeKafka() {
        PropertyReader.setRuntimeKafkaEnabled(false);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                PropertyReader.setRuntimeKafkaEnabled(true);
                timer.cancel();
            }
        }, Long.parseLong(PropertyReader.getKafkaProperties().get(Properties.RUN_TIME_KAFKA_FRESHNESS_THRESHOLD)));
    }

    @Override
    public void run() {
        if(Boolean.TRUE.equals(Boolean.valueOf(PropertyReader.getKafkaProperties().get(Properties.HEALTH_CHECK_ACTIVE)))) {
            HealthCheckHttpClient healthCheckClient = new HealthCheckHttpClient();
            //assuming it takes 20 secs to complete the task
            if (log.isDebugEnabled()) {
                log.debug("Executing logstash health check");
            }
            healthCheckClient.kafkaConsumerCheckHealth();
        }
    }
}
