package com.wso2telco.scheduler;

import com.wso2telco.util.PropertyReader;

import java.util.Timer;
import java.util.TimerTask;

import static com.wso2telco.util.Constants.CONSUMER_HEALTHCHEACK_FRESHNESS_THRESHOLD;
import static com.wso2telco.util.Constants.RUNTIMEKAFKA_FRESHNESS_THRESHOLD;

public class ScheduleTimerTask extends TimerTask {

    public static void runTimerHealthCheck() {
        TimerTask timerTask = new ScheduleTimerTask();
        //running timer task as daemon thread
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(timerTask, 0, CONSUMER_HEALTHCHEACK_FRESHNESS_THRESHOLD);
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
        }, RUNTIMEKAFKA_FRESHNESS_THRESHOLD);
    }

    @Override
    public void run() {
        completeTask();
    }

    private void completeTask() {
        try {
            //assuming it takes 20 secs to complete the task
            PropertyReader.getHealthCheckClient().kafkaConsumerCheckHealth();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
