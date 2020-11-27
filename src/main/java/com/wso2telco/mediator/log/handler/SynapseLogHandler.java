package com.wso2telco.mediator.log.handler;

import com.wso2telco.kafka.MessageSender;
import com.wso2telco.util.LogHandlerUtil;
import com.wso2telco.util.PropertyReader;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static com.wso2telco.util.CommonConstant.*;
import static com.wso2telco.util.Constants.*;

public class SynapseLogHandler extends AbstractSynapseHandler implements ManagedLifecycle {

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
        try {
            PropertyReader.setInitialized(true);
            String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + FILE_NAME;
            File fXmlFile = new File(configPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(fXmlFile);
            document.getDocumentElement().normalize();
            NodeList requestinAttributes = document.getElementsByTagName(REQUEST_IN.toUpperCase());
            PropertyReader.setLogProperties(requestinAttributes, REQUEST_IN);
            NodeList requestoutAttributes = document.getElementsByTagName(REQUEST_OUT.toUpperCase());
            PropertyReader.setLogProperties(requestoutAttributes, REQUEST_OUT);
            NodeList responseinAttributes = document.getElementsByTagName(RESPONSE_IN.toUpperCase());
            PropertyReader.setLogProperties(responseinAttributes, RESPONSE_IN);
            NodeList responseoutAttributes = document.getElementsByTagName(RESPONSE_OUT.toUpperCase());
            PropertyReader.setLogProperties(responseoutAttributes, RESPONSE_OUT);
            NodeList kafkaAttributes = document.getElementsByTagName(KAFKA_CONFIGURATION.toUpperCase());
            PropertyReader.setLogProperties(kafkaAttributes, KAFKA_CONFIGURATION);
        } catch (SAXException | ParserConfigurationException | IOException e) {
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
                org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
                logProperties(messageContext, axis2MessageContext, REQUEST_IN);
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
                org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
                logProperties(messageContext, axis2MessageContext, REQUEST_OUT);
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
                org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
                logProperties(messageContext, axis2MessageContext, RESPONSE_IN);
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
                org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
                logProperties(messageContext, axis2MessageContext, RESPONSE_OUT);
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

    private void logProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, String typeFlag) throws IOException, XMLStreamException {


        String transactionPayload = "";
        Map<String, Object> headerMap = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        StringBuilder transactionLog = new StringBuilder("TRANSACTION:" + typeFlag + LOGMESSAGEDELIMITER + "TIMESTAMP" + LOGDATADELIMITER + timestamp.getTime());
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
                    transactionLog.append(LOGMESSAGEDELIMITER).append(entry.getKey()).append(LOGDATADELIMITER).append(messageContext.getProperty(key));
                } else if (value.equalsIgnoreCase(AX)) {
                    transactionLog.append(LOGMESSAGEDELIMITER).append(entry.getKey()).append(LOGDATADELIMITER).append(axis2MessageContext.getProperty(key));
                } else if (value.equalsIgnoreCase(TH)) {
                    transactionLog.append(LOGMESSAGEDELIMITER).append(entry.getKey()).append(LOGDATADELIMITER).append(headerMap.get(key));
                } else {
                    transactionLog.append(LOGMESSAGEDELIMITER).append(entry.getKey()).append(LOGDATADELIMITER).append(transactionPayload.replaceAll("\n", "")).append(LOGDATADELIMITER).append(entry.getKey());
                }
            }

        }
        MessageSender messageSender = new MessageSender();
        messageSender.sendMessage(transactionLog.toString());

    }

}
