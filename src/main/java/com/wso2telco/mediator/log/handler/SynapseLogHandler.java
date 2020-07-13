package com.wso2telco.mediator.log.handler;

import com.wso2telco.util.LogHandlerUtil;
import org.apache.synapse.AbstractSynapseHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.json.JSONObject;

import static com.wso2telco.util.CommonConstant.*;

public class SynapseLogHandler extends AbstractSynapseHandler {

    /*
     * Incoming request to the service or API. This is the first entry point,
     * in fact it is after Axis2 layer. This is where we will determine the
     * tracking id and log HTTP method and headers similar to wire log.
     *
     */
    public boolean handleRequestInFlow(MessageContext messageContext) {
        try {
            logMessage(messageContext, "request_in");
        } catch (Exception e) {
            MEDIATOR_LOGGER.error("Error while reading message context : " + e.getMessage());
        }
        return true;
    }

    /*
     * Outgoing request from the service to the backend. This is where we will
     * log the outgoing HTTP address and headers.
     *
     */
    public boolean handleRequestOutFlow(MessageContext messageContext) {
        try {
            logMessage(messageContext, "request_out");
        } catch (Exception e) {
            MEDIATOR_LOGGER.error("Unable to set log context due to : " + e.getMessage());
        }
        return true;
    }

    /*
     * Incoming response from backend to service. This is where we will
     * log the backend response headers and status.
     *
     */
    public boolean handleResponseInFlow(MessageContext messageContext) {
        try {
            logMessage(messageContext, "response_in");
        } catch (Exception e) {
            MEDIATOR_LOGGER.error("Unable to set log context due to : " + e.getMessage());
        }
        return true;
    }

    /*
     * Outgoing response from the service to caller. This is where we will log
     * the service response header and status.
     *
     */
    public boolean handleResponseOutFlow(MessageContext messageContext) {
        try {
            logMessage(messageContext, "response_out");
        } catch (Exception e) {
            MEDIATOR_LOGGER.error("Unable to set log context due to : " + e.getMessage());
        } finally {
            LogHandlerUtil.clearLogContext();
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
            if (JsonUtil.hasAJsonPayload(((Axis2MessageContext) messageContext).getAxis2MessageContext())) {
                JSONObject jsonPayload = new JSONObject(JsonUtil.jsonPayloadToString(((Axis2MessageContext) messageContext).getAxis2MessageContext()));
                payload = jsonPayload.toString();
            } else {
                payload = messageContext.getEnvelope().toString();
            }
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
                payload = messageContext.getEnvelope().toString();
            }
        } catch (Exception e) {
            payload = "payload dropped due to invalid format";
        } finally {
            return payload;
        }
    }


    private void logMessage(MessageContext messageContext, String flowDirection) {
        String API_NAME = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        String HTTP_METHOD = LogHandlerUtil.getHTTPMethod(messageContext);// messageContext.getProperty(Constants.Configuration.HTTP_METHOD);
        String CONTEXT = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String FULL_REQUEST_PATH = (String) messageContext.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String HTTP_RESPONSE_STATUS_CODE = LogHandlerUtil.getHTTPStatusMessage(messageContext);
        String SUB_PATH = (String) messageContext.getProperty(RESTConstants.REST_SUB_REQUEST_PATH);
        String ERROR_CODE = String.valueOf(messageContext.getProperty(SynapseConstants.ERROR_CODE));
        String ERROR_MESSAGE = (String) messageContext.getProperty(SynapseConstants.ERROR_MESSAGE);
        String requestPayload = null;
        String requestId = null;

        if (flowDirection.equalsIgnoreCase("request_in")) {
            messageContext.setProperty(INFLOW_REQUEST_START_TIME, System.currentTimeMillis());
            requestPayload = handleInPayload(messageContext);
            requestId = LogHandlerUtil.generateTrackingId(messageContext);
        }

        if (flowDirection.equalsIgnoreCase("request_out")) {
            messageContext.setProperty(OUTFLOW_REQUEST_START_TIME, System.currentTimeMillis());
            requestPayload = handleOutPayload(messageContext);
            requestId = messageContext.getProperty(TRACKING_MESSAGE_ID).toString();
        }

        if (flowDirection.equalsIgnoreCase("response_in")) {
            messageContext.setProperty(INFLOW_RESPONSE_END_TIME, System.currentTimeMillis());
            requestPayload = handleInPayload(messageContext);
            requestId = messageContext.getProperty(TRACKING_MESSAGE_ID).toString();
        }

        if (flowDirection.equalsIgnoreCase("response_out")) {
            requestPayload = handleOutPayload(messageContext);
            long serviceTime, esbTime = 0, backendTime = 0, backendEndTime = 0;
            long endTime = System.currentTimeMillis();
            long startTime = 0, backendStartTime = 0;
            if (messageContext.getProperty(INFLOW_REQUEST_START_TIME) != null) {
                startTime = (Long) messageContext.getProperty(INFLOW_REQUEST_START_TIME);
            }
            if (messageContext.getProperty(OUTFLOW_REQUEST_START_TIME) != null) {
                backendStartTime = (Long) messageContext.getProperty(OUTFLOW_REQUEST_START_TIME);
            }
            if (messageContext.getProperty(INFLOW_RESPONSE_END_TIME) != null) {
                backendEndTime = (Long) messageContext.getProperty(INFLOW_RESPONSE_END_TIME);
            }

            serviceTime = endTime - startTime;
            //When start time not properly set
            if (startTime == 0) {
                backendTime = 0;
                esbTime = 0;
            } else if (endTime != 0 && backendStartTime != 0 && backendEndTime != 0) { //When
                // response caching is disabled
                backendTime = backendEndTime - backendStartTime;
                esbTime = serviceTime - backendTime;
            } else if (endTime != 0 && backendStartTime == 0) {//When response caching enabled
                backendTime = 0;
                esbTime = serviceTime;
            }
            MEDIATOR_LOGGER.info("TRANSACTION:" + flowDirection + ",API_REQUEST_ID:" + messageContext.getProperty(TRACKING_MESSAGE_ID) + "" +
                    ", HTTP_STATUS: " + HTTP_RESPONSE_STATUS_CODE + ", SERVICE_TIME: " + serviceTime +
                    ", BACKEND_TIME: " + backendTime + ", ESB_TIME: " + esbTime +
                    ",BODY:" + requestPayload);
        } else {
            MEDIATOR_LOGGER.info("TRANSACTION:" + flowDirection + ",API_REQUEST_ID:" + requestId + "" +
                    ",API_NAME:" + API_NAME + "" +
                    ",API_CONTEXT:" + CONTEXT +
                    ",API_RESOURCE_PATH:" + FULL_REQUEST_PATH +
                    ",METHOD:" + HTTP_METHOD +
                    ",BODY:" + requestPayload);
        }
    }

}
