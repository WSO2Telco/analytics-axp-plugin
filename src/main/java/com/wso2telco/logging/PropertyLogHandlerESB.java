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

    private static final Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");
    private static final Log billingLogHandler = LogFactory.getLog("BILLING_REQUEST_RESPONSE_LOGGER");
	private static final Log logger = LogFactory.getLog(PropertyLogHandlerESB.class.getName());
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
        String apiOperation = setOperation(operation);
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
            	logger.error("Error while converting axis2MessageContext"+e.getMessage());
            }
        }
        else{
            jsonBody = JsonUtil.jsonPayloadToString(axis2MessageContext);
            FAULT = "false";
        }

        if (direction.equals("nb response")) {
        	StringBuilder lognbResponseBuilder = new StringBuilder();
        	StringBuilder billingLognbResponseBuilder = new StringBuilder();
        	lognbResponseBuilder.append("NORTHBOUND_RESPONSE_LOGGER-API_REQUEST_ID:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(REQUESTID));
        	lognbResponseBuilder.append("-wso2telco_value,APPLICATION_ID:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(APPLICATION_ID));
        	lognbResponseBuilder.append("-wso2telco_value,API_NAME:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(API_NAME));
        	lognbResponseBuilder.append("-wso2telco_value,API_VERSION:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(API_VERSION));
        	lognbResponseBuilder.append("-wso2telco_value,RESOURSE:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(RESOURCE));
        	lognbResponseBuilder.append("-wso2telco_value,RESPONSE_TIME:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(RESPONSE_TIME));
        	lognbResponseBuilder.append("-wso2telco_value,OPERATION:wso2telco_value:");
        	lognbResponseBuilder.append(apiOperation);
        	lognbResponseBuilder.append("-wso2telco_value,USER_ID:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(USER_ID));
        	lognbResponseBuilder.append("-wso2telco_value,DIRECTION:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(DIRECTION));
        	lognbResponseBuilder.append("-wso2telco_value,OPERATOR_NAME:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(OPERATOR_NAME));
        	lognbResponseBuilder.append("-wso2telco_value,OPERATOR_ID:wso2telco_value:");
        	lognbResponseBuilder.append(messageContext.getProperty(OPERATOR_ID));
        	lognbResponseBuilder.append("-wso2telco_value,Body:wso2telco_value:");
        	lognbResponseBuilder.append(jsonBody.replaceAll("\n", ""));
        	lognbResponseBuilder.append("-wso2telco_value,ERROR:");
        	lognbResponseBuilder.append(FAULT);
            logHandler.info(lognbResponseBuilder);
            billingLognbResponseBuilder.append("NORTHBOUND_RESPONSE_LOGGER-API_REQUEST_ID:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(REQUESTID));
            billingLognbResponseBuilder.append("-wso2telco_value,API:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(API_NAME));
            billingLognbResponseBuilder.append("-wso2telco_value,RESOURCE_PATH:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(RESOURCE));
            billingLognbResponseBuilder.append("-wso2telco_value,METHOD:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(REST_METHOD));
            billingLognbResponseBuilder.append("-wso2telco_value,RESPONSE_TIME:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(RESPONSE_TIME));
            billingLognbResponseBuilder.append("-wso2telco_value,API_PUBLISHER:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(API_PUBLISHER));
            billingLognbResponseBuilder.append("-wso2telco_value,APPLICATION_NAME:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(APPLICATION_NAME));
            billingLognbResponseBuilder.append("-wso2telco_value,REQUEST_ID:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(REQUESTID));
            billingLognbResponseBuilder.append("-wso2telco_value,DIRECTION:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(DIRECTION));
            billingLognbResponseBuilder.append("-wso2telco_value,JSON_BODY:wso2telco_value:");
            billingLognbResponseBuilder.append(jsonBody.replaceAll("\\s", ""));
            billingLognbResponseBuilder.append("-wso2telco_value,API_ID:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(API_ID));
            billingLognbResponseBuilder.append("-wso2telco_value,APPLICATION_ID:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(APPLICATION_ID));
            billingLognbResponseBuilder.append("-wso2telco_value,OPERATOR_NAME:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(OPERATOR_NAME));
            billingLognbResponseBuilder.append("-wso2telco_value,OPERATOR_ID:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(OPERATOR_ID));
            billingLognbResponseBuilder.append("-wso2telco_value,SP_USER_ID:wso2telco_value:");
            billingLognbResponseBuilder.append(messageContext.getProperty(USER_ID));
            billingLogHandler.info(billingLognbResponseBuilder);
        }
        else if(direction.equals("sb response")){
        	StringBuilder logsbResponseBuilder = new StringBuilder();
        	StringBuilder billingSbLognbResponseBuilder = new StringBuilder();
        	logsbResponseBuilder.append("SOUTHBOUND_RESPONSE_LOGGER-API_REQUEST_ID:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(REQUESTID));
        	logsbResponseBuilder.append("-wso2telco_value,APPLICATION_ID:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(APPLICATION_ID));
        	logsbResponseBuilder.append("-wso2telco_value,API_NAME:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(API_NAME));
        	logsbResponseBuilder.append("-wso2telco_value,API_VERSION:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(API_VERSION));
        	logsbResponseBuilder.append("-wso2telco_value,RESOURSE:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(RESOURCE));
        	logsbResponseBuilder.append("-wso2telco_value,RESPONSE_TIME:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(RESPONSE_TIME));
        	logsbResponseBuilder.append("-wso2telco_value,OPERATION:wso2telco_value:");
        	logsbResponseBuilder.append(apiOperation);
        	logsbResponseBuilder.append("-wso2telco_value,USER_ID:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(USER_ID));
        	logsbResponseBuilder.append("-wso2telco_value,DIRECTION:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(DIRECTION));
        	logsbResponseBuilder.append("-wso2telco_value,OPERATOR_NAME:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(OPERATOR_NAME));
        	logsbResponseBuilder.append("-wso2telco_value,OPERATOR_ID:wso2telco_value:");
        	logsbResponseBuilder.append(messageContext.getProperty(OPERATOR_ID));
        	logsbResponseBuilder.append("-wso2telco_value,Body:wso2telco_value:");
        	logsbResponseBuilder.append(jsonBody.replaceAll("\n", ""));
        	logsbResponseBuilder.append("-wso2telco_value,ERROR:");
        	logsbResponseBuilder.append(FAULT);
            logHandler.info(logsbResponseBuilder);
            billingSbLognbResponseBuilder.append("SOUTHBOUND_RESPONSE_LOGGER-API_REQUEST_ID:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(REQUESTID));
            billingSbLognbResponseBuilder.append("-wso2telco_value,API:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(API_NAME));
            billingSbLognbResponseBuilder.append("-wso2telco_value,RESOURCE_PATH:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(RESOURCE));
            billingSbLognbResponseBuilder.append("-wso2telco_value,METHOD:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(REST_METHOD));
            billingSbLognbResponseBuilder.append("-wso2telco_value,RESPONSE_TIME:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(RESPONSE_TIME));
            billingSbLognbResponseBuilder.append("-wso2telco_value,API_PUBLISHER:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(API_PUBLISHER));
            billingSbLognbResponseBuilder.append("-wso2telco_value,APPLICATION_NAME:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(APPLICATION_NAME));
            billingSbLognbResponseBuilder.append("-wso2telco_value,REQUEST_ID:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(REQUESTID));
            billingSbLognbResponseBuilder.append("-wso2telco_value,DIRECTION:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(DIRECTION));
            billingSbLognbResponseBuilder.append("-wso2telco_value,JSON_BODY:wso2telco_value:");
            billingSbLognbResponseBuilder.append(jsonBody.replaceAll("\\s", ""));
            billingSbLognbResponseBuilder.append("-wso2telco_value,API_ID:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(API_ID));
            billingSbLognbResponseBuilder.append("-wso2telco_value,APPLICATION_ID:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(APPLICATION_ID));
            billingSbLognbResponseBuilder.append("-wso2telco_value,OPERATOR_NAME:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(OPERATOR_NAME));
            billingSbLognbResponseBuilder.append("-wso2telco_value,OPERATOR_ID:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(OPERATOR_ID));
            billingSbLognbResponseBuilder.append("-wso2telco_value,SP_USER_ID:wso2telco_value:");
            billingSbLognbResponseBuilder.append(messageContext.getProperty(USER_ID));
            billingLogHandler.info(billingSbLognbResponseBuilder);

        }
    }

}
