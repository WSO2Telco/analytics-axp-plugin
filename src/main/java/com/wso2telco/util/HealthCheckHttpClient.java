package com.wso2telco.util;

import com.wso2telco.scheduler.ScheduleTimerTask;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.wso2telco.util.Constants.*;

public class HealthCheckHttpClient {


    private String healthCheckUrl = "http://" + HEALTHCHECK_HOST + ":" + HEALTHCHECK_PORT + "/v3/kafka/local/consumer/" + HEALTHCHECK_CONSUMER_ID + "/status";


    public void kafkaConsumerCheckHealth() throws IOException {
        try{
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(healthCheckUrl);
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent()));

            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();
            JSONObject obj = new JSONObject(response.toString());
            double totalLag = obj.getJSONObject("status").getDouble("totallag");
            if (totalLag > HEALTHCHECK_TOTALLAG && PropertyReader.isRuntimeKafkaEnabled()) {
                ScheduleTimerTask.runTimerDisableRuntimeKafka();
            }
            httpClient.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

    }

}
