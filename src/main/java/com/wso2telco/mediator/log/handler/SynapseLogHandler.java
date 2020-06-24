package com.wso2telco.mediator.log.handler;

import org.apache.axis2.Constants;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import java.io.InputStream;
import java.io.StringWriter;

public class SynapseLogHandler extends AbstractSynapseHandler {
    private static final Log logHandler = LogFactory.getLog("MEDIATOR_LOGGER");


    private static final String SELOG_INFLOW_REQUEST_START_TIME = "IFRST";
    private static final String SELOG_OUTFLOW_REQUEST_START_TIME = "ORST";
    private static final String SELOG_INFLOW_RESPONSE_END_TIME = "IFRET";

    /*
     * Incoming request to the service or API. This is the first entry point,
     * in fact it is after Axis2 layer. This is where we will determine the
     * tracking id and log HTTP method and headers similar to wire log.
     *
     */
    public boolean handleRequestInFlow(MessageContext messageContext) {
        String API_NAME = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String HTTP_METHOD = (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        String CONTEXT = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String FULL_REQUEST_PATH = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        //SynapseLog log = LogHandlerUtil.getLog(messageContext, LOGGER);
        messageContext.setProperty(SELOG_INFLOW_REQUEST_START_TIME, System.currentTimeMillis());

        try {
            //Set the logging context
           //LogHandlerUtil.setLogContext(messageContext, log);
            String requestPayload = handleInPayload(messageContext);
            logHandler.info("TRANSACTION:request_in,API_REQUEST_ID:" + LogHandlerUtil.generateTrackingId(messageContext) + "" +
                    ",API_NAME:" + API_NAME + "" +
                    ",API_CONTEXT:" + CONTEXT +
                    ",API_RESOURCE_PATH:" + FULL_REQUEST_PATH +
                    ",METHOD:" + HTTP_METHOD +
                    ",BODY:" + requestPayload + "\n");
        } catch (Exception e) {
            logHandler.error("Error while reading message context : " + e.getMessage());
        }
        return true;
    }

    /**
     * method used to handle invalid payloads
     */
    private String handleInPayload(MessageContext messageContext) {
        String payload = "";
        try {
            RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
            InputStream jsonPaylodStream = (InputStream) ((Axis2MessageContext) messageContext)
                    .getAxis2MessageContext().getProperty("org.apache.synapse.commons.json.JsonInputStream");
            StringWriter writer = new StringWriter();
            IOUtils.copy(jsonPaylodStream, writer);
            payload = writer.toString();
        } catch (Exception e) {
            payload = "payload dropped due to invalid format";
        } finally {
            return payload;
        }
    }

    /**
     * method used to handle invalid payloads
     */
    private String handleOutPayload(MessageContext messageContext) {
        String payload = "";
        try {
            if (JsonUtil.hasAJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext())) {
                payload = JsonUtil.jsonPayloadToString(((Axis2MessageContext) messageContext).getAxis2MessageContext());
            } else {
                payload =  messageContext.getEnvelope().toString();
            }
            //payload = messageContext.getEnvelope().getBody().toString();
        } catch (Exception e) {
            payload = "payload dropped due to invalid format";
        } finally {
            return payload;
        }
    }

    /*
     * Outgoing request from the service to the backend. This is where we will
     * log the outgoing HTTP address and headers.
     *
     */
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        String API_NAME = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String HTTP_METHOD = (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        String CONTEXT = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String FULL_REQUEST_PATH = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        messageContext.setProperty(SELOG_OUTFLOW_REQUEST_START_TIME, System.currentTimeMillis());
        String requestPayload = handleOutPayload(messageContext);
        try {
            logHandler.info("TRANSACTION:request_out,API_REQUEST_ID:" + messageContext.getProperty(LogHandlerUtil.TRACKING_MESSAGE_ID) + "" +
                    ",API_NAME:" + API_NAME + "" +
                    ",API_CONTEXT:" + CONTEXT +
                    ",API_RESOURCE_PATH:" + FULL_REQUEST_PATH +
                    ",METHOD:" + HTTP_METHOD +
                    ",BODY:" + requestPayload + "\n");
        } catch (Exception e) {
            logHandler.error("Unable to set log context due to : " + e.getMessage());
        }
        return true;
    }

    /*
     * Incoming response from backend to service. This is where we will
     * log the backend response headers and status.
     *
     */
    public boolean handleResponseInFlow(MessageContext messageContext) {
        String API_NAME = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String HTTP_METHOD = (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        String CONTEXT = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String FULL_REQUEST_PATH = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        //SynapseLog log = LogHandlerUtil.getLog(messageContext, LOGGER);
        messageContext.setProperty(SELOG_INFLOW_RESPONSE_END_TIME, System.currentTimeMillis());
        String requestPayload = handleInPayload(messageContext);
        try {
            logHandler.info("TRANSACTION:response_in,API_REQUEST_ID:" + messageContext.getProperty(LogHandlerUtil.TRACKING_MESSAGE_ID) + "" +
                    ",API_NAME:" + API_NAME + "" +
                    ",API_CONTEXT:" + CONTEXT +
                    ",API_RESOURCE_PATH:" + FULL_REQUEST_PATH +
                    ",METHOD:" + HTTP_METHOD +
                    ",BODY:" + requestPayload + "\n");
        } catch (Exception e) {
            logHandler.error("Unable to set log context due to : " + e.getMessage());
        }
        return true;
    }

    /*
     * Outgoing response from the service to caller. This is where we will log
     * the service response header and status.
     *
     */
    public boolean handleResponseOutFlow(MessageContext messageContext) {

        //SynapseLog log = LogHandlerUtil.getLog(messageContext, LOGGER);
        String requestPayload = handleOutPayload(messageContext);
        long responseTime, serviceTime = 0, backendTime = 0, backendEndTime = 0;
        long endTime = System.currentTimeMillis();

        try {
            long startTime = 0, backendStartTime = 0;
            if (messageContext.getProperty(SELOG_INFLOW_REQUEST_START_TIME) != null) {
                startTime = (Long) messageContext.getProperty(SELOG_INFLOW_REQUEST_START_TIME);
            }

            if (messageContext.getProperty(SELOG_OUTFLOW_REQUEST_START_TIME) != null) {
                backendStartTime = (Long) messageContext.getProperty(SELOG_OUTFLOW_REQUEST_START_TIME);
            }

            if (messageContext.getProperty(SELOG_INFLOW_RESPONSE_END_TIME) != null) {
                backendEndTime = (Long)messageContext.getProperty(SELOG_INFLOW_RESPONSE_END_TIME);
            }

            responseTime = endTime - startTime;
            //When start time not properly set
            if (startTime == 0) {
                backendTime = 0;
                serviceTime = 0;
            } else if (endTime != 0 && backendStartTime != 0 && backendEndTime != 0) { //When
                // response caching is disabled
                backendTime = backendEndTime - backendStartTime;
                serviceTime = responseTime - backendTime;
            } else if (endTime != 0 && backendStartTime == 0) {//When response caching enabled
                backendTime = 0;
                serviceTime = responseTime;
            }

            String API_NAME = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
            String HTTP_METHOD = (String) messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
            String CONTEXT = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
            String FULL_REQUEST_PATH = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            String SUB_PATH = (String) messageContext.getProperty(RESTConstants.REST_SUB_REQUEST_PATH);
            String HTTP_RESPONSE_STATUS_CODE = LogHandlerUtil.getHTTPStatusMessage(messageContext);
            String ERROR_CODE = String.valueOf(messageContext.getProperty(SynapseConstants.ERROR_CODE));
            String ERROR_MESSAGE = (String) messageContext.getProperty(SynapseConstants.ERROR_MESSAGE);

            logHandler.info( "\n"+"TRANSACTION:response_out,API_REQUEST_ID:" + messageContext.getProperty(LogHandlerUtil.TRACKING_MESSAGE_ID) + "" +
                    ",API_NAME:" + API_NAME + "" +
                    ",API_CONTEXT:" + CONTEXT +
                    ",API_RESOURCE_PATH:" + FULL_REQUEST_PATH +
                    ",METHOD:" + HTTP_METHOD +
                    ", HTTP_RESPONSE_STATUS_CODE: " + HTTP_RESPONSE_STATUS_CODE + ", RESPONSE_TIME: " + responseTime +
                    ", BACKEND_TIME: " + backendTime + ", SERVICE_TIME: " + serviceTime +
                    ",BODY:" + requestPayload);

        } catch (Exception e) {
            logHandler.error("Unable to set log context due to : " + e.getMessage());
        } finally {
            LogHandlerUtil.clearLogContext();
        }
        return true;
    }
}
