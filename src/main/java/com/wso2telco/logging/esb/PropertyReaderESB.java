package com.wso2telco.logging.esb;

import org.w3c.dom.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PropertyReaderESB {

    private static final String REQUESTIN = "REQUESTIN";
    private static final String REQUESTOUT = "REQUESTOUT";
    private static final String RESPONSEIN = "RESPONSEIN";
    private static final String RESPONSEOUT = "RESPONSEOUT";
    private static HashMap<String, String> requestinpropertyMap = new LinkedHashMap();//
    private static HashMap<String, String> requestoutpropertyMap = new LinkedHashMap();//
    private static HashMap<String, String> responseinpropertyMap = new LinkedHashMap();//
    private static HashMap<String, String> responseoutpropertyMap = new LinkedHashMap();//
    private static HashMap<String, String> errorpropertyMap = new LinkedHashMap();//

    private static final Logger LOGGER = Logger.getLogger("loggingExtention");

    public static HashMap<String, String> getRequestInpropertyMap() { return requestinpropertyMap; }

    public static HashMap<String, String> getRequestOutpropertyMap() { return requestoutpropertyMap; }

    public static HashMap<String, String> getResponseInpropertyMap() { return responseinpropertyMap; }

    public static HashMap<String, String> getResponseOutpropertyMap() { return responseoutpropertyMap; }

    public static HashMap<String, String> getErrorPropertiesMap() { return errorpropertyMap; }

    /**
     * Set the transaction properties
     * @param requestNodeList the request attribute list
     * @param flag transaction type
     */
    public static void setLogProperties(NodeList requestNodeList, String flag) {
        LOGGER.info("Setting up Log Properties for : "+ flag);
        Node requestNode = requestNodeList.item(0);
        if (requestNode.getNodeType() == Node.ELEMENT_NODE) {
            Element requestElement = (Element) requestNode;
            String requestEl = requestElement.getTextContent().trim();
            String requestArray[] = requestEl.split("\n");
            if (flag.equalsIgnoreCase(REQUESTIN)) {
                for (int x = 0; x < requestArray.length; x++) {
                    requestinpropertyMap.put(requestElement.getElementsByTagName("*").item((x)).getNodeName(), requestArray[x].trim());
                }
            }else if (flag.equalsIgnoreCase(REQUESTOUT)) {
                for (int x = 0; x < requestArray.length; x++) {
                    requestoutpropertyMap.put(requestElement.getElementsByTagName("*").item((x)).getNodeName(), requestArray[x].trim());
                }
            }else if (flag.equalsIgnoreCase(RESPONSEIN)) {
                for (int y = 0; y < requestArray.length; y++) {
                    responseinpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                }
            }else if (flag.equalsIgnoreCase(RESPONSEOUT)) {
                for (int y = 0; y < requestArray.length; y++) {
                    responseoutpropertyMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                }
            }else {
                for (int z = 0; z < requestArray.length; z++) {
                    errorpropertyMap.put(requestElement.getElementsByTagName("*").item((z)).getNodeName(), requestArray[z].trim());
                }
            }
        }
    }

}
