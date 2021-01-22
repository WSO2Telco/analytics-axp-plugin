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

import com.wso2telco.util.PropertyReader;
import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.XML;

import java.util.HashMap;
import java.util.Map;

import static com.wso2telco.util.CommonConstant.*;

public class PropertyLogHandler extends AbstractMediator implements ManagedLifecycle {

    private static String nullOrTrimmed(String inputString) {
        String result = null;
        if (inputString != null && inputString.trim().length() > 0) {
            result = inputString.trim();
        }
        return result;
    }

    /**
     * within this method read the XML file and pass the attribute
     *
     * @param synapseEnvironment
     */
    public void init(SynapseEnvironment synapseEnvironment) {
        PropertyReader propertyReader = new PropertyReader();
        propertyReader.readGatewayTransactionProperties();
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
        if (log.isDebugEnabled()) {
            log.debug("Message ID: " + messageContext.getMessageID() + " Payload Enabled: "+ isPayloadLoggingEnabled +
                    " Direction: "+direction);
        }
        if (direction != null) {
            if (direction.equalsIgnoreCase(REQUEST)) {
                logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, REQUEST);
            } else if (direction.equalsIgnoreCase(RESPONSE)) {
                logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, RESPONSE);
            } else if (direction.equalsIgnoreCase(ERROR)) {
                logProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled, ERROR_RESPONSE);
            }
            return true;
        } else {
             return false;
        }
    }

    /**
     * method used to set the print the log
     *
     * @param messageContext
     * @param axis2MessageContext
     * @param isPayloadLoggingEnabled
     * @param typeFlag                type of the transaction
     */
    private void logProperties(MessageContext messageContext, org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled, String typeFlag) {

        if (isPayloadLoggingEnabled) {
            String transactionPayload = handleAndReturnPayload(messageContext);
            Map<String, Object> headerMap = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
            StringBuilder transactionLog = new StringBuilder("TRANSACTION:" + typeFlag);
            HashMap<String, String> transactionMap;
            if (typeFlag.equals(REQUEST)) {
                transactionMap = PropertyReader.getRequestpropertyMap();
            } else if (typeFlag.equals(RESPONSE)) {
                transactionMap = PropertyReader.getResponsepropertyMap();
            } else {
                transactionMap = PropertyReader.getErrorpropertyMap();
            }

            /**Check the request map and recall the init method */
            if (transactionMap.isEmpty()) {
                init(null);
            }
            for (Map.Entry<String, String> entry : transactionMap.entrySet()) {

                String key = entry.getValue().split(String.valueOf(','))[0];
                String value = entry.getValue().split(String.valueOf(','))[1];


                if (value.equalsIgnoreCase(MC)) {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(messageContext.getProperty(key));
                } else if (value.equalsIgnoreCase(AX)) {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(axis2MessageContext.getProperty(key));
                } else if (value.equalsIgnoreCase(TH)) {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(headerMap.get(key));
                } else {
                    transactionLog.append(LOG_MESSAGE_DELIMITER).append(entry.getKey()).append(LOG_DATA_DELIMITER).append(transactionPayload.replace("\n", ""));
                }


            }
            REQUEST_RESPONSE_LOGGER.info(transactionLog);
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

    /**
     * method used to handle invalid payloads
     *
     * @param messageContext
     * @return the payload
     */
    private String handleAndReturnPayload(MessageContext messageContext) {
        String payload = "";
        try {
            payload = messageContext.getEnvelope().getBody().toString();
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Message ID: " + messageContext.getMessageID() + " Error while getting message payload "+ e.getMessage());
            }
            payload = "payload dropped due to invalid format";
        }
        return payload;

    }


}