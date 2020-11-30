package com.wso2telco.util;


import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.wso2telco.util.CommonConstant.*;
import static com.wso2telco.util.Constants.AM_MAPPING_ID;


@Getter
public class PropertyReader {
    @Getter
    public static boolean requestInEnabled = false;
    @Getter
    public static boolean requestOutEnabled = false;
    @Getter
    public static boolean responseInEnabled = false;
    @Getter
    public static boolean responseOutEnabled = false;
    @Getter
    @Setter
    public static boolean initialized = false;
    @Getter
    private static HashMap<String, String> requestpropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> responsepropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> errorpropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> requestinpropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> responseinpropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> requestoutpropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> responseoutpropertyMap = new LinkedHashMap<>();
    @Getter
    private static HashMap<String, String> kafkaPropertyMap = new LinkedHashMap<>();
    private PropertyReader() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Set the transaction properties
     *
     * @param requestNodeList the request attribute list
     * @param flag            transaction type
     */
    public static void setLogProperties(NodeList requestNodeList, String flag) {
        Node requestNode = requestNodeList.item(0);
        if (requestNode.getNodeType() == Node.ELEMENT_NODE) {
            Element requestElement = (Element) requestNode;
            String requestEl = requestElement.getTextContent().trim();
            String[] requestArray = requestEl.split("\n");
            Map<String, String> transactionMap = new LinkedHashMap<>();
            switch (flag) {
                case REQUEST:
                    transactionMap = requestpropertyMap;
                    break;
                case RESPONSE:
                    transactionMap = responsepropertyMap;
                    break;
                case ERROR_RESPONSE:
                    transactionMap = errorpropertyMap;
                    break;
                case REQUEST_IN:
                    transactionMap = requestinpropertyMap;
                    requestInEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case REQUEST_OUT:
                    transactionMap = requestoutpropertyMap;
                    requestOutEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case RESPONSE_IN:
                    transactionMap = responseinpropertyMap;
                    responseInEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case RESPONSE_OUT:
                    transactionMap = responseoutpropertyMap;
                    responseOutEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case KAFKA_CONFIGURATION:
                    setKafkaProperties(requestArray, requestElement);
                    kafkaEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                default:
                    AXP_ANALYTICS_LOGGER.error("Error in Set log Properties" + flag);
            }
            if(!flag.equalsIgnoreCase(KAFKA_CONFIGURATION)) {
                for (int y = 0; y < requestArray.length; y++) {
                    transactionMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                }
            }

        }
    }

    static void setKafkaProperties(String[] requestArray, Element requestElement) {
        for (int y = 0; y < requestArray.length; y++) {
            switch (requestElement.getElementsByTagName("*").item((y)).getNodeName()) {
                case "KAFKA_HOST":
                    KAFKA_HOST = requestArray[y].trim();
                    break;
                case "KAFKA_PORT":
                    KAFKA_PORT = requestArray[y].trim();
                    break;
                case "RETRIES_CONFIG":
                    RETRIES_CONFIG = requestArray[y].trim();
                    break;
                case "TRANSACTION_TIMEOUT_CONFIG":
                    TRANSACTION_TIMEOUT_CONFIG = requestArray[y].trim();
                    break;
                case "KAFKA_TOPIC":
                    KAFKA_TOPIC = requestArray[y].trim();
                    break;
                case "MAX_THREAD_COUNT":
                    MAX_THREAD_COUNT = requestArray[y].trim();
                    break;

            }
        }
    }

}
