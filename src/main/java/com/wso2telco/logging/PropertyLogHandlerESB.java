package com.wso2telco.logging;

import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

public class PropertyLogHandlerESB extends AbstractMediator{

    Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");
    private static final String DIRECTION = "DIRECTION";
    private static final String APPLICATION_ID = "APPLICATION_ID";
    private static final String REQUESTID = "REQUEST_ID";
    private static final String API_NAME = "API_NAME";
    private static final String API_VERSION ="VERSION";
    private static final String RESOURCE ="RESOURCE";
    private static final String USER_ID ="USER_ID";
    private static final String RESPONSE_TIME= "RESPONSE_TIME";
    private static final String OPERATOR_NAME = "OPERATOR_NAME";
    private static final String OPERATOR_ID = "OPERATOR_ID";
    private static String OPERATION = null;

    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        String direction = (String) messageContext.getProperty(DIRECTION);
        logResponsePropertiesESB(messageContext, axis2MessageContext,direction);
        return true;

    }

    private void logResponsePropertiesESB(MessageContext messageContext,
                                       org.apache.axis2.context.MessageContext axis2MessageContext, String direction) {

        String operation = (String) messageContext.getProperty("HANDLER");
        
        if(operation.equals("AmountChargeHandler")){
            OPERATION ="Charge";
        }
        else if (operation.equals("AmountRefundHandler")){
            OPERATION = "Refund";
        }
        String jsonBody = JsonUtil.jsonPayloadToString(axis2MessageContext);
        if (direction.equals("nb response")) {
            logHandler.info("NORTHBOUND_RESPONSE_LOGGER-"+"API_REQUEST_ID:" + messageContext.getProperty(REQUESTID) +
                    ",APPLICATION_ID:" + (String) messageContext.getProperty(APPLICATION_ID) +
                    ",API_NAME:" + messageContext.getProperty(API_NAME) +
                    ",API_VERSION:" + messageContext.getProperty(API_VERSION) +
                    ",RESOURSE:" + messageContext.getProperty(RESOURCE) +
                    ",RESPONSETIME:" + messageContext.getProperty(RESPONSE_TIME)+
                    ",OPERATION:" + OPERATION +
                    ",USER_ID:" + messageContext.getProperty(USER_ID) +
                    ",DIRECTION:" + messageContext.getProperty(DIRECTION) +
                    ",OPERATORNAME:" + messageContext.getProperty(OPERATOR_NAME) +
                    ",OPERATORID:" + messageContext.getProperty(OPERATOR_ID) +
                    ",Body:" + jsonBody.replaceAll("\n", ""));
        }
        else if(direction.equals("sb response")){
            logHandler.info("SOUTHBOUND_RESPONSE_LOGGER-"+"API_REQUEST_ID:" + messageContext.getProperty(REQUESTID) +
                    ",APPLICATION_ID:" + (String) messageContext.getProperty(APPLICATION_ID) +
                    ",API_NAME:" + messageContext.getProperty(API_NAME) +
                    ",API_VERSION:" + messageContext.getProperty(API_VERSION) +
                    ",RESOURSE:" + messageContext.getProperty(RESOURCE) +
                    ",RESPONSETIME:" + messageContext.getProperty(RESPONSE_TIME)+
                    ",OPERATION:" + OPERATION +
                    ",USER_ID:" + messageContext.getProperty(USER_ID) +
                    ",DIRECTION:" + messageContext.getProperty(DIRECTION) +
                    ",OPERATORNAME:" + messageContext.getProperty(OPERATOR_NAME) +
                    ",OPERATORID:" + messageContext.getProperty(OPERATOR_ID) +
                    ",Body:" + jsonBody.replaceAll("\n", ""));
        }

    }

}
