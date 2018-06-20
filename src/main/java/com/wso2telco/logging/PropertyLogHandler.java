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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.axiom.om.impl.llom.OMTextImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.commons.json.JsonUtil;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

public class PropertyLogHandler extends AbstractMediator {

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static final String REGISTRY_PATH = "gov:/apimgt/";
	private static final String REQUEST_ID = "mife.prop.requestId";
	private static final String MESSAGE_TYPE = "message.type";
	private static final String PAYLOAD_LOGGING_ENABLED = "payload.logging.enabled";
	private static final String APPLICATION_ID = "api.ut.application.id";
	private static final String API_PUBLISHER = "api.ut.apiPublisher";
	private static final String API_NAME = "API_NAME";
	private static final String REQUEST = "request";
	private static final String RESPONSE = "response";
	private static final String API_VERSION ="SYNAPSE_REST_API_VERSION";
	private static final String API_CONTEXT = "api.ut.context";
	private static final String USER_ID="api.ut.userId";

	private static final Log logHandler = LogFactory.getLog("REQUEST_RESPONSE_LOGGER");

	public boolean mediate(MessageContext messageContext) {

		boolean isPayloadLoggingEnabled = false;

		org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
				.getAxis2MessageContext();

		isPayloadLoggingEnabled = extractPayloadLoggingStatus(messageContext);

		String direction = (String) axis2MessageContext.getProperty(MESSAGE_TYPE);

		if (direction != null && direction.equalsIgnoreCase(REQUEST)) {
			logRequestProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled);
		} else if (direction != null && direction.equalsIgnoreCase(RESPONSE)) {
			logResponseProperties(messageContext, axis2MessageContext, isPayloadLoggingEnabled);
		}

		return true;
	}

	private void logRequestProperties(MessageContext messageContext,
			org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled) {

		String requestId = (String) messageContext.getProperty(REQUEST_ID);

		TreeMap<String,String> headers = (TreeMap<String, String>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
		String jwzToken=headers.get("X-JWT-Assertion");

		if (nullOrTrimmed(requestId) == null) {
			UniqueIDGenerator.generateAndSetUniqueID("MI", messageContext,
					(String) messageContext.getProperty(APPLICATION_ID));
		}

		logHandler.info("API_REQUEST_ID:"+messageContext.getProperty(REQUEST_ID)+",APPLICATION_ID:"+(String) messageContext.getProperty(APPLICATION_ID)+",API_NAME:"+messageContext.getProperty(API_NAME)+",API_PUBLISHER:"+
				messageContext.getProperty(API_PUBLISHER)+",API_VERSION,"+messageContext.getProperty(API_VERSION)
				+",API_CONTEXT:"+messageContext.getProperty(API_CONTEXT)+",USER_ID:"+messageContext.getProperty(USER_ID)+"jwzToken"+jwzToken);
		
		if (isPayloadLoggingEnabled) {
			String jsonBody = JsonUtil.jsonPayloadToString(axis2MessageContext);
			logHandler.info("API_REQUEST_ID:"+messageContext.getProperty(REQUEST_ID)+">>>>> reqBody :" + jsonBody);
		}

	}

	private void logResponseProperties(MessageContext messageContext,
			org.apache.axis2.context.MessageContext axis2MessageContext, boolean isPayloadLoggingEnabled) {


		logHandler.info("[" + dateFormat.format(new Date()) + "] <<<<< API Request id "
				+ messageContext.getProperty(REQUEST_ID));
		
		if (isPayloadLoggingEnabled) {
			String jsonBody = JsonUtil.jsonPayloadToString(axis2MessageContext);
			logHandler.info("API_REQUEST_ID:"+messageContext.getProperty(REQUEST_ID)+" <<<<< respBody :" + jsonBody);
		}

	}
	
	private boolean extractPayloadLoggingStatus (MessageContext messageContext) {
		boolean isPayloadLoggingEnabled = false;
		
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
}
