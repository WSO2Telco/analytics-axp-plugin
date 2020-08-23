package com.wso2telco.util;


import lombok.Getter;
import lombok.Setter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedHashMap;


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
    @Getter@Setter
    public static boolean initialized=false;


    private static HashMap<String, String> requestpropertyMap = new LinkedHashMap();
    private static HashMap<String, String> responsepropertyMap = new LinkedHashMap();
    private static HashMap<String, String> errorpropertyMap = new LinkedHashMap();
    @Getter
    private static HashMap<String, String> requestinpropertyMap = new LinkedHashMap();
    @Getter
    private static HashMap<String, String> responseinpropertyMap = new LinkedHashMap();
    @Getter
    private static HashMap<String, String> requestoutpropertyMap = new LinkedHashMap();
    @Getter
    private static HashMap<String, String> responseoutpropertyMap = new LinkedHashMap();

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
            switch (flag) {
                case (CommonConstant.REQUEST):
                    for (int y = 0; y < requestArray.length; y++) {
                        requestpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                    }
                    break;
                case (CommonConstant.RESPONSE):
                    for (int y = 0; y < requestArray.length; y++) {
                        responsepropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                    }
                    break;
                case (CommonConstant.ERROR_RESPONSE):
                    for (int y = 0; y < requestArray.length; y++) {
                        errorpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                    }
                    break;
                case (CommonConstant.REQUEST_IN):
                    for (int y = 0; y < requestArray.length; y++) {
                        requestinpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                        requestInEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem("enabled").getNodeValue());
                    }
                    break;
                case (CommonConstant.REQUEST_OUT):
                    for (int y = 0; y < requestArray.length; y++) {
                        requestoutpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                        requestOutEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem("enabled").getNodeValue());
                    }
                    break;
                case (CommonConstant.RESPONSE_IN):
                    for (int y = 0; y < requestArray.length; y++) {
                        responseinpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                        responseInEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem("enabled").getNodeValue());
                    }
                    break;
                case (CommonConstant.RESPONSE_OUT):
                    for (int y = 0; y < requestArray.length; y++) {
                        responseoutpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                        responseOutEnabled = Boolean.parseBoolean(requestNode.getAttributes().getNamedItem("enabled").getNodeValue());
                    }
                    break;

            }

        }
    }

}
