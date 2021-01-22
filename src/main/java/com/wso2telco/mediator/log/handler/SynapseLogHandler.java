package com.wso2telco.mediator.log.handler;

import com.wso2telco.kafka.MessageSender;
import com.wso2telco.scheduler.ScheduleTimerTask;
import com.wso2telco.util.LogHandlerUtil;
import com.wso2telco.util.Properties;
import com.wso2telco.util.PropertyReader;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONObject;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static com.wso2telco.util.CommonConstant.*;

public class SynapseLogHandler extends AbstractSynapseHandler implements ManagedLifecycle {

    private static final Log log =  LogFactory.getLog(SynapseLogHandler.class);
    ExecutorService executor = null;
    MessageSender messageSender = null;
    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        try {

            PropertyReader propertyReader = new PropertyReader();
            propertyReader.readKafkaProperties();
            propertyReader.readMediatorTransactionProperties();
            PropertyReader.setInitialized(true);
            executor = Executors.newFixedThreadPool(Integer.parseInt(PropertyReader.getKafkaProperties().
                    get(Properties.MAX_THREAD_COUNT)));
            messageSender = new MessageSender(executor);
            ScheduleTimerTask.runTimerHealthCheck();
            if (log.isDebugEnabled()) {
                log.debug("SynapseLogHandler initialization finished");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
        throw new UnsupportedOperationException();
    }

    /*
     * Incoming request to the service or API. This is the first entry point,
     * in fact it is after Axis2 layer. This is where we will determine the
     * tracking id and log HTTP method and headers similar to wire log.
     *
     */
    public boolean handleRequestInFlow(MessageContext messageContext) {
        /**Check the init method has initialized or recall the init method */
        if (!PropertyReader.isInitialized()) {
            init(null);
        }
        if (PropertyReader.isRequestInEnabled()) {
            try {
                logProperties(messageContext, REQUEST_IN);
            } catch (Exception e) {
                AXP_ANALYTICS_LOGGER.error("Error while reading message context : " + e.getMessage());
            }
        }
        return true;
    }

    /*
     * Outgoing request from the service to the backend. This is where we will
     * log the outgoing HTTP address and headers.
     *
     */
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        if (PropertyReader.isRequestOutEnabled()) {
            try {
                logProperties(messageContext, REQUEST_OUT);
            } catch (Exception e) {
                AXP_ANALYTICS_LOGGER.error(ERRORINLOGGING + e.getMessage());
            }
        }
        return true;
    }

    /*
     * Incoming response from backend to service. This is where we will
     * log the backend response headers and status.
     *
     */
    public boolean handleResponseInFlow(MessageContext messageContext) {
        if (PropertyReader.isResponseInEnabled()) {
            try {
                logProperties(messageContext, RESPONSE_IN);
            } catch (Exception e) {
                AXP_ANALYTICS_LOGGER.error(ERRORINLOGGING + e.getMessage());
            }
        }
        return true;
    }

    /*
     * Outgoing response from the service to caller. This is where we will log
     * the service response header and status.
     *
     */
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        if (PropertyReader.isResponseOutEnabled()) {
            try {
                logProperties(messageContext, RESPONSE_OUT);
            } catch (Exception e) {
                AXP_ANALYTICS_LOGGER.error(ERRORINLOGGING + e.getMessage());
            }
        }
        LogHandlerUtil.clearLogContext();
        return true;
    }

    /**
     * method used to handle invalid payloads
     */
    private String handleInPayload(MessageContext messageContext) {
        String payload = "";
        try {
            RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
            if (JsonUtil.hasAJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext())) {
                JSONObject jsonPayload = new JSONObject(JsonUtil.jsonPayloadToString(((Axis2MessageContext) messageContext).getAxis2MessageContext()));
                payload = jsonPayload.toString();
            } else {
                payload = messageContext.getEnvelope().toString();
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Message ID: " + messageContext.getMessageID() + " Error while getting message payload "+ e.getMessage());
            }
            payload = "payload dropped due to invalid format";
        }
        return payload;
    }

    /**
     * method used to handle invalid payloads
     */
    private String handleOutPayload(MessageContext messageContext) {
        String payload = "";
        try {
            if (JsonUtil.hasAJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext())) {
                JSONObject jsonPayload = new JSONObject(JsonUtil.jsonPayloadToString(((Axis2MessageContext) messageContext).getAxis2MessageContext()));
                payload = jsonPayload.toString();
            } else {
                payload = messageContext.getEnvelope().toString();
            }
        } catch (Exception e) {
            payload = "payload dropped due to invalid format";
        }
        return payload;

    }

    private void logProperties(MessageContext messageContext,  String typeFlag) throws IOException, XMLStreamException {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String transactionPayload = "";
        Map<String, Object> headerMap = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        StringBuilder transactionLog = new StringBuilder("TRANSACTION:" + typeFlag + LOG_MESSAGE_DELIMITER + "TIMESTAMP" + LOG_DATA_DELIMITER + timestamp.getTime());
        HashMap<String, String> transactionMap = null;

        switch (typeFlag) {
            case (REQUEST_IN):
                transactionMap = PropertyReader.getRequestinpropertyMap();
                transactionPayload = handleInPayload(messageContext);
                break;
            case (REQUEST_OUT):
                transactionMap = PropertyReader.getRequestoutpropertyMap();
                transactionPayload = handleOutPayload(messageContext);
                break;
            case (RESPONSE_IN):
                transactionMap = PropertyReader.getResponseinpropertyMap();
                transactionPayload = handleInPayload(messageContext);
                break;
            case (RESPONSE_OUT):
                transactionMap = PropertyReader.getResponseoutpropertyMap();
                transactionPayload = handleOutPayload(messageContext);
                break;
            default:
                transactionMap = null;

        }

        if(transactionMap == null)
            return;
        /*Check the request map and recall the init method */
        if (!PropertyReader.isInitialized()) {
            init(null);
        }

        for (Map.Entry<String, String> entry : transactionMap.entrySet()) {

            String key = entry.getValue().split(String.valueOf(','))[0];
            String value = entry.getValue().split(String.valueOf(','))[1];

            if (null == messageContext.getProperty(MESSAGE_ID) && AM_MAPPING_ID.equalsIgnoreCase(entry.getKey())) {
                LogHandlerUtil.generateTrackingId(messageContext, key, value);
            } else {
                if (value.equalsIgnoreCase(MC)) {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(messageContext.getProperty(key));
                } else if (value.equalsIgnoreCase(AX)) {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(axis2MessageContext.getProperty(key));
                } else if (value.equalsIgnoreCase(TH)) {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(headerMap.get(key));
                } else {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(transactionPayload.replace("\n", "")).append(LOG_DATA_DELIMITER).append(entry.getKey());
                }
            }

        }
        messageSender.sendMessage(transactionLog.toString());

    }

}
