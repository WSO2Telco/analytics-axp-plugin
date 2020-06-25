package com.wso2telco.util;


import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class PropertyReader {

    private static HashMap<String, String> requestpropertyMap = new LinkedHashMap();
    private static HashMap<String, String> responsepropertyMap = new LinkedHashMap();
    private static HashMap<String, String> errorpropertyMap = new LinkedHashMap();

    public static HashMap<String, String> getRequestpropertyMap() {
        return requestpropertyMap;
    }

    public static HashMap<String, String> getResponsepropertyMap() {
        return responsepropertyMap;
    }

    public static HashMap<String, String> getErrorPropertiesMap() {
        return errorpropertyMap;
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
            String requestArray[] = requestEl.split("\n");

            if (flag.equalsIgnoreCase(CommonConstant.REQUEST)) {
                for (int x = 0; x < requestArray.length; x++) {
                    requestpropertyMap.put(requestElement.getElementsByTagName("*").item((x)).getNodeName(), requestArray[x].trim());
                }
            } else if (flag.equalsIgnoreCase(CommonConstant.RESPONSE)) {
                for (int y = 0; y < requestArray.length; y++) {
                    responsepropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                }
            } else {
                for (int z = 0; z < requestArray.length; z++) {
                    errorpropertyMap.put(requestElement.getElementsByTagName("*").item((z)).getNodeName(), requestArray[z].trim());
                }
            }
        }
    }

}
