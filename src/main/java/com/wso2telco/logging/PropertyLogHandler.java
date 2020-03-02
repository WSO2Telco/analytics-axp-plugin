/*******************************************************************************
 * Copyright  (c) 2015-2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 *
 * WSO2.Telco Inc. licences this file to you under  the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.wso2telco.logging;

import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PropertyLogHandler extends AbstractMediator implements ManagedLifecycle {
    private static final String REGISTRY_PATH = "gov:/apimgt/";
    private static final String MESSAGE_TYPE = "message.type";
    private static final String PAYLOAD_LOGGING_ENABLED = "payload.logging.enabled";
    private static final String REQUEST = "request";
    private static final String RESPONSE = "response";
    private static final String ERRORRESPONSE = "errorResponse";
    private static final String UUID = "MESSAGE_ID";
    private static final String ERROR = "error";
    private static final String REST_SUB_REQUEST_PATH = "REST_SUB_REQUEST_PATH";
    private static final String API_RESOURCE_CACHE_KEY = "API_RESOURCE_CACHE_KEY";
    private static final String CONTENT_TYPE = "messageType";
    private static final Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");
    private static final String MC = "MC";
    private static final String AX = "AX";
    private static final String TH = "TH";
    private static final String FILE_NAME = "logManagerConfig.xml";


    /**
     * within this method read the XML file and pass the attribute
     * @param synapseEnvironment
     */
    public void init(SynapseEnvironment synapseEnvironment) {
        try {
            String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + FILE_NAME;
            File fXmlFile = new File(configPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(fXmlFile);
            document.getDocumentElement().normalize();
            NodeList requestAttributes = document.getElementsByTagName(REQUEST.toUpperCase());
            PropertyReader.setLogProperties(requestAttributes, REQUEST);
            NodeList responseAttributes = document.getElementsByTagName(RESPONSE.toUpperCase());
            PropertyReader.setLogProperties(responseAttributes, RESPONSE);
            NodeList errorAttributes = document.getElementsByTagName(ERRORRESPONSE.toUpperCase());
            PropertyReader.setLogProperties(errorAttributes, ERRORRESPONSE);

        }
        catch (ParserConfigurationException e ) {
            e.printStackTrace();
        } catch (SAXException er) {
            er.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void destroy() {
        throw new UnsupportedOperationException();
    }


    public boolean mediate(MessageContext messageContext) {
        //This variable need to read from registry, for a now directly initiating the variable
        boolean isPayloadLoggingEnabled = true;
        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        isPayloadLoggingEnabled = extractPayloadLoggingStatus(messageContext);

        String direction = (String) axis2MessageContext.getProperty(MESSAGE_TYPE);
        if (direction != null) {
            if (direction.equalsIgnoreCase(REQUEST)) {
                logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, REQUEST);
            } else if (direction.equalsIgnoreCase(RESPONSE)) {
                logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, RESPONSE);
            } else if (direction.equalsIgnoreCase(ERROR)) {
                logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, ERRORRESPONSE);
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * method used to set the print the log
     * @param messageContext
     * @param axis2MessageContext
     * @param isPayloadLoggingEnabled
     * @param typeFlag type of the transaction
     */
    private void logProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled, String typeFlag) {

        if (isPayloadLoggingEnabled) {
            String transactionPayload = handleAndReturnPayload(messageContext);
            Map<String, Object> headerMap = (Map<String, Object>)axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            StringBuilder transactionLog = new StringBuilder("TRANSACTION:" + typeFlag);
            HashMap<String, String> transactionMap;
            if (typeFlag.equals(REQUEST)) {
                transactionMap = PropertyReader.getRequestpropertyMap();
            } else if (typeFlag.equals(RESPONSE)) {
                transactionMap = PropertyReader.getResponsepropertyMap();
            } else {
                transactionMap = PropertyReader.getErrorPropertiesMap();
            }

            /**Check the request map and recall the init method */
            if (transactionMap.isEmpty()) {
                init(null);
            }


            for (String i : transactionMap.keySet()) {
                if (transactionMap.get(i).split(",")[1].equalsIgnoreCase(MC)) {
                    transactionLog.append("," + i + ":" + messageContext.getProperty(transactionMap.get(i).split(",")[0]));
                } else if (transactionMap.get(i).split(",")[1].equalsIgnoreCase(AX)) {
                    transactionLog.append("," + i + ":" + axis2MessageContext.getProperty(transactionMap.get(i).split(",")[0]));
                } else if (transactionMap.get(i).split(",")[1].equalsIgnoreCase(TH)) {
                        transactionLog.append("," + i + ":" +headerMap.get(transactionMap.get(i).split(",")[0]));
                } else {
                    transactionLog.append("," + i + ":" + transactionPayload.replaceAll("\n", ""));
                }
            }
            logHandler.info(transactionLog);
        }
    }


    private boolean extractPayloadLoggingStatus(MessageContext messageContext) {
        boolean isPayloadLoggingEnabled = true;
        Entry payloadEntry = new Entry(REGISTRY_PATH + PAYLOAD_LOGGING_ENABLED);
        OMTextImpl payloadEnableRegistryValue = (OMTextImpl) messageContext.getConfiguration().getRegistry()
                .getResource(payloadEntry, null);
        if (payloadEnableRegistryValue != null) {
            String payloadLogEnabled = payloadEnableRegistryValue.getText();

            if (nullOrTrimmed(payloadLogEnabled) != null) {
                isPayloadLoggingEnabled = Boolean.valueOf(payloadLogEnabled);
            }
        }
        return isPayloadLoggingEnabled;
    }

    private static String nullOrTrimmed(String inputString) {
        String result = null;
        if (inputString != null && inputString.trim().length() > 0) {
            result = inputString.trim();
        }
        return result;
    }


    /**
     * method used to handle invalid payloads
     * @param messageContext
     * @return the payload
     */
    private String handleAndReturnPayload(MessageContext messageContext) {
        String payload = "";
        try {
            payload = messageContext.getEnvelope().getBody().toString();
        } catch (Exception e) {
            e.printStackTrace();
            payload = "payload dropped due to invalid format";
        }
        return payload;

    }

    /**
     * this method can be used if we need to get extract only json as body
     **/
    @SuppressWarnings("unused")
    private String getPayloadSting(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext) {
        String payload;
        if (axis2MessageContext.getProperty(CONTENT_TYPE).equals("application/json")) {
            /**if content type is json */
            payload = XML.toJSONObject(messageContext.getEnvelope().getBody().getFirstElement().getFirstElement().toString()).toString();
        } else if (axis2MessageContext.getProperty(CONTENT_TYPE).equals("text/plain")) {
            /**if content type is text/plain */
            payload = messageContext.getEnvelope().getBody().getFirstElement().toString();
        } else {
            /** if content type is xml */
            payload = messageContext.getEnvelope().getBody().toString();
        }
        return payload;
    }


}