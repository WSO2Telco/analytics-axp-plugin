package com.wso2telco.logging;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONObject;
import org.json.XML;

public class PropertyLogHandlerESB extends AbstractMediator{

    Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");
	Log billingLogHandler = LogFactory.getLog("BILLING_REQUEST_RESPONSE_LOGGER");
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
    private static final String API_ID = "API_ID";
    private static final String APPLICATION_NAME = "APPLICATION_NAME";
    private static final String API_PUBLISHER = "API_PUBLISHER";
    private static final String REST_METHOD = "REST_METHOD";
    private static String FAULT = null;


    public boolean mediate(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        String direction = (String) messageContext.getProperty(DIRECTION);
        logResponsePropertiesESB(messageContext, axis2MessageContext,direction);
        return true;

    }

    public String setOperation(String handler) {

        switch (handler){
            case "AmountChargeHandler":
                handler ="Charge";
                break;
            case "AmountRefundHandler":
                handler = "Refund";
                break;
            case  "RetrieveSMSHandler" : case "RetrieveSMSSouthboundHandler" : case "RetrieveSMSNorthboundHandler":
                handler = "ReceiveSMS";
                break;
            case "SendSMSHandler" :
                handler = "sendSMS";
                break;
            case "QuerySMSStatusHandler":
                handler = "DeliveryInfo";
                break;
            case  "OutboundSMSSubscriptionsNorthboundHandler" : case "SMSOutboundNotificationsHandler" :
                handler = "SubscribeToDeliveryNotifications";
                break;
            case  "SMSInboundSubscriptionsNorthboundHandler" : case "SMSInboundNotificationsHandler" :
                handler = "SubscribetoMessageNotifcations";
                break;
            case "StopOutboundSMSSubscriptionsSouthBoundHandler":
                handler = "SubscribeToDeliveryNotifications";
                break;
            case "StopInboundSMSSubscriptionsHandler":
                handler = "StopSubscriptionToMessageNotifcations";
                break;
        }
        return handler;

    }

    private void logResponsePropertiesESB(MessageContext messageContext,
                                       org.apache.axis2.context.MessageContext axis2MessageContext, String direction) {

        String operation = (String) messageContext.getProperty("HANDLER");
        String API_OPERATION = setOperation(operation);
        String errorCode = (String) messageContext.getProperty("ERROR_CODE");
        String jsonBody =null;
        
        if(errorCode != null){
            try {
                String xmlString = axis2MessageContext.getEnvelope().toString();
                JSONObject xmlJsonObj = XML.toJSONObject(xmlString);
                jsonBody = xmlJsonObj.toString();
                FAULT = "true";
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            jsonBody = JsonUtil.jsonPayloadToString(axis2MessageContext);
            FAULT = "false";
        }

        if (direction.equals("nb response")) {
            logHandler.info("NORTHBOUND_RESPONSE_LOGGER-"+"API_REQUEST_ID:wso2telco_value:" +  messageContext.getProperty(REQUESTID) +
                    "-wso2telco_value,APPLICATION_ID:wso2telco_value:" + (String) messageContext.getProperty(APPLICATION_ID) +
                    "-wso2telco_value,API_NAME:wso2telco_value:" + messageContext.getProperty(API_NAME) +
                    "-wso2telco_value,API_VERSION:wso2telco_value:" + messageContext.getProperty(API_VERSION) +
                    "-wso2telco_value,RESOURSE:wso2telco_value:" + messageContext.getProperty(RESOURCE) +
                    "-wso2telco_value,RESPONSE_TIME:wso2telco_value:" + messageContext.getProperty(RESPONSE_TIME)+
                    "-wso2telco_value,OPERATION:wso2telco_value:" + API_OPERATION +
                    "-wso2telco_value,USER_ID:wso2telco_value:" + messageContext.getProperty(USER_ID) +
                    "-wso2telco_value,DIRECTION:wso2telco_value:" + messageContext.getProperty(DIRECTION) +
                    "-wso2telco_value,OPERATOR_NAME:wso2telco_value:" + messageContext.getProperty(OPERATOR_NAME) +
                    "-wso2telco_value,OPERATOR_ID:wso2telco_value:" + messageContext.getProperty(OPERATOR_ID) +
                    "-wso2telco_value,Body:wso2telco_value:" + jsonBody.replaceAll("\n", "") +
                    "-wso2telco_value,ERROR:" + FAULT);
            billingLogHandler.info("NORTHBOUND_RESPONSE_LOGGER-"+"API_REQUEST_ID:wso2telco_value:" +  messageContext.getProperty(REQUESTID) +
                    "-wso2telco_value,API:wso2telco_value:" + messageContext.getProperty(API_NAME) +
                    "-wso2telco_value,RESOURCE_PATH:wso2telco_value:" + messageContext.getProperty(RESOURCE) +
                    "-wso2telco_value,METHOD:wso2telco_value:" + messageContext.getProperty(REST_METHOD) +
                    "-wso2telco_value,RESPONSE_TIME:wso2telco_value:" + messageContext.getProperty(RESPONSE_TIME)+
                    "-wso2telco_value,API_PUBLISHER:wso2telco_value:" + messageContext.getProperty(API_PUBLISHER) +
                    "-wso2telco_value,APPLICATION_NAME:wso2telco_value:" + messageContext.getProperty(APPLICATION_NAME) +
                    "-wso2telco_value,REQUEST_ID:wso2telco_value:" + messageContext.getProperty(REQUESTID) +
                    "-wso2telco_value,DIRECTION:wso2telco_value:" + messageContext.getProperty(DIRECTION) +
                    "-wso2telco_value,JSON_BODY:wso2telco_value:" + jsonBody.replaceAll("\\s", "") +
                    "-wso2telco_value,API_ID:wso2telco_value:" + messageContext.getProperty(API_ID) +
                    "-wso2telco_value,APPLICATION_ID:wso2telco_value:" + messageContext.getProperty(APPLICATION_ID) +
                    "-wso2telco_value,OPERATOR_NAME:wso2telco_value:" + messageContext.getProperty(OPERATOR_NAME) +
                    "-wso2telco_value,OPERATOR_ID:wso2telco_value:" + messageContext.getProperty(OPERATOR_ID) +
                    "-wso2telco_value,SP_USER_ID:wso2telco_value:" + messageContext.getProperty(USER_ID));
        }
        else if(direction.equals("sb response")){
            logHandler.info("SOUTHBOUND_RESPONSE_LOGGER-"+"API_REQUEST_ID:wso2telco_value:" + messageContext.getProperty(REQUESTID) +
                    "-wso2telco_value,APPLICATION_ID:wso2telco_value:" + (String) messageContext.getProperty(APPLICATION_ID) +
                    "-wso2telco_value,API_NAME:wso2telco_value:" + messageContext.getProperty(API_NAME) +
                    "-wso2telco_value,API_VERSION:wso2telco_value:" + messageContext.getProperty(API_VERSION) +
                    "-wso2telco_value,RESOURSE:wso2telco_value:" + messageContext.getProperty(RESOURCE) +
                    "-wso2telco_value,RESPONSE_TIME:wso2telco_value:" + messageContext.getProperty(RESPONSE_TIME)+
                    "-wso2telco_value,OPERATION:wso2telco_value:" + API_OPERATION +
                    "-wso2telco_value,USER_ID:wso2telco_value:" + messageContext.getProperty(USER_ID) +
                    "-wso2telco_value,DIRECTION:wso2telco_value:" + messageContext.getProperty(DIRECTION) +
                    "-wso2telco_value,OPERATOR_NAME:wso2telco_value:" + messageContext.getProperty(OPERATOR_NAME) +
                    "-wso2telco_value,OPERATOR_ID:wso2telco_value:" + messageContext.getProperty(OPERATOR_ID) +
                    "-wso2telco_value,Body:wso2telco_value:" + jsonBody.replaceAll("\n", "") +
                    "-wso2telco_value,ERROR:" + FAULT);
            billingLogHandler.info("SOUTHBOUND_RESPONSE_LOGGER-"+"API_REQUEST_ID:wso2telco_value:" +  messageContext.getProperty(REQUESTID) +
                    "-wso2telco_value,API:wso2telco_value:" + messageContext.getProperty(API_NAME) +
                    "-wso2telco_value,RESOURCE_PATH:wso2telco_value:" + messageContext.getProperty(RESOURCE) +
                    "-wso2telco_value,METHOD:wso2telco_value:" + messageContext.getProperty(REST_METHOD) +
                    "-wso2telco_value,RESPONSE_TIME:wso2telco_value:" + messageContext.getProperty(RESPONSE_TIME)+
                    "-wso2telco_value,API_PUBLISHER:wso2telco_value:" + messageContext.getProperty(API_PUBLISHER) +
                    "-wso2telco_value,APPLICATION_NAME:wso2telco_value:" + messageContext.getProperty(APPLICATION_NAME) +
                    "-wso2telco_value,REQUEST_ID:wso2telco_value:" + messageContext.getProperty(REQUESTID) +
                    "-wso2telco_value,DIRECTION:wso2telco_value:" + messageContext.getProperty(DIRECTION) +
                    "-wso2telco_value,JSON_BODY:wso2telco_value:" + jsonBody.replaceAll("\\s", "") +
                    "-wso2telco_value,API_ID:wso2telco_value:" + messageContext.getProperty(API_ID) +
                    "-wso2telco_value,APPLICATION_ID:wso2telco_value:" + messageContext.getProperty(APPLICATION_ID) +
                    "-wso2telco_value,OPERATOR_NAME:wso2telco_value:" + messageContext.getProperty(OPERATOR_NAME) +
                    "-wso2telco_value,OPERATOR_ID:wso2telco_value:" + messageContext.getProperty(OPERATOR_ID) +
                    "-wso2telco_value,SP_USER_ID:wso2telco_value:" + messageContext.getProperty(USER_ID));

        }
    }

}
