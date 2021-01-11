package com.wso2telco.util;

import com.wso2telco.scheduler.ScheduleTimerTask;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HealthCheckHttpClient {

    private static final String KAFKA_CONSUMER_OBJECT = "status";
    private static final String KAFKA_CONSUMER_STATUS_STRING = "status";
    private static final String KAFKA_CONSUMER_STATUS = "OK";

    public void kafkaConsumerCheckHealth() {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse closeableHttpResponse = null;

        try{
            String healthCheckUrl = buildHealthCheckUrl();
            httpClient = HttpClients.createDefault();
            RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).setConnectionRequestTimeout(5000).build();
            HttpGet httpGet = new HttpGet(healthCheckUrl);
            httpGet.setConfig(requestConfig);
            closeableHttpResponse = httpClient.execute(httpGet);

            int statusCode = closeableHttpResponse.getStatusLine().getStatusCode();

            if(statusCode == HttpStatus.SC_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        closeableHttpResponse.getEntity().getContent()));

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                JSONObject obj = new JSONObject(response.toString());
                String kafkaConsumerStatus = obj.getJSONObject(KAFKA_CONSUMER_OBJECT).getString(KAFKA_CONSUMER_STATUS_STRING);
                if (kafkaConsumerStatus.equalsIgnoreCase(KAFKA_CONSUMER_STATUS) && PropertyReader.isRuntimeKafkaEnabled()) {
                    ScheduleTimerTask.runTimerDisableRuntimeKafka();
                }
            } else {
                //TODO re-schedule the http call again
                System.out.println("re-schedule the http call again.................in one minute");
            }
        } catch (Exception e) {
            //TODO log the exception in log file
            //log.error("Error while generating refresh token , ", e);
        }
        finally {
            try {
                if(closeableHttpResponse != null) {
                    closeableHttpResponse.close();
                }
            } catch (IOException e) {
                //TODO log the exception
                e.printStackTrace();
            }
        }
    }

    private String buildHealthCheckUrl() {
        StringBuilder healthCheckUrl = new StringBuilder("http://");
        healthCheckUrl.append(PropertyReader.getKafkaProperties().get(Properties.HEALTH_CHECK_HOST))
                .append(":")
                .append(PropertyReader.getKafkaProperties().get(Properties.HEALTH_CHECK_PORT))
                .append("/v3/kafka/local/consumer/")
                .append(PropertyReader.getKafkaProperties().get(Properties.HEALTH_CHECK_CONSUMER_ID))
                .append("/status");
        return healthCheckUrl.toString();
    }

}
