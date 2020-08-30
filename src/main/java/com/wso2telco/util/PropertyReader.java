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
                case (CommonConstant.REQUEST):
                    transactionMap = requestpropertyMap;
                    break;
                case (CommonConstant.RESPONSE):
                    transactionMap = responsepropertyMap;
                    break;
                case (CommonConstant.ERROR_RESPONSE):
                    transactionMap = errorpropertyMap;
                    break;
                case (CommonConstant.REQUEST_IN):
                    transactionMap = requestinpropertyMap;
                    requestInEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case (CommonConstant.REQUEST_OUT):
                    transactionMap = requestoutpropertyMap;
                    requestOutEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case (CommonConstant.RESPONSE_IN):
                    transactionMap = responseinpropertyMap;
                    responseInEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                case (CommonConstant.RESPONSE_OUT):
                    transactionMap = responseoutpropertyMap;
                    responseOutEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem(ENABLED).getNodeValue());
                    break;
                default:
                    AXP_ANALYTICS_LOGGER.error("Error in Set log Properties" + flag);
            }

            for (int y = 0; y < requestArray.length; y++) {
                transactionMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
            }

        }
    }

}
