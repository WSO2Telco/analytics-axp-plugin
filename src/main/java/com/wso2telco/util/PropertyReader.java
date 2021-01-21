package com.wso2telco.util;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Properties;

import static com.wso2telco.util.CommonConstant.*;

@Getter
public class PropertyReader {
    private static final Log log =  LogFactory.getLog(PropertyReader.class);
    @Getter
    static HashMap<String, String> kafkaProperties = new HashMap<>();
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
    @Getter@Setter
    private static TimeOutCount errorCount=new TimeOutCount();
    @Getter@Setter
    private static boolean runtimeKafkaEnabled=true;
    @Getter@Setter
    private static long runtimeKafkaUpdateMillis=Long.MAX_VALUE;


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
                 default:
                    AXP_ANALYTICS_LOGGER.error("Error in Set log Properties" + flag);
            }
                for (int y = 0; y < requestArray.length; y++) {
                    transactionMap.put(requestElement.getElementsByTagName("*").item((y)).getNodeName(), requestArray[y].trim());
                }
        }
    }

    public void readKafkaProperties() {

        java.util.Properties prop = new Properties();
        InputStream input = null;

        try {
            String absolutePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + CommonConstant.KAFKA_CONFIGURATION_FILE;
            input = new FileInputStream(absolutePath);

            // load a properties file
            prop.load(input);
            Enumeration<?> e = prop.propertyNames();
            while (e.hasMoreElements()) {
                String key = (String) e.nextElement();
                String value = prop.getProperty(key);
                System.out.println("key" + key + "           ........... value "+ value);
                kafkaProperties.put(key, value);
            }

        } catch (IOException ex) {
            log.error("Error while reading property file "+ ex.getMessage());
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("Error while closing file reading operation " + e.getMessage());
                }
            }
        }

    }

    public void readMediatorTransactionProperties() {
        try{
            String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + CommonConstant.TRANSACTION_PROPERTY_CONFIGURATION_FILE;
            File fXmlFile = new File(configPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(fXmlFile);
            document.getDocumentElement().normalize();
            NodeList requestinAttributes = document.getElementsByTagName(REQUEST_IN.toUpperCase());
            PropertyReader.setLogProperties(requestinAttributes, REQUEST_IN);
            NodeList requestoutAttributes = document.getElementsByTagName(REQUEST_OUT.toUpperCase());
            PropertyReader.setLogProperties(requestoutAttributes, REQUEST_OUT);
            NodeList responseinAttributes = document.getElementsByTagName(RESPONSE_IN.toUpperCase());
            PropertyReader.setLogProperties(responseinAttributes, RESPONSE_IN);
            NodeList responseoutAttributes = document.getElementsByTagName(RESPONSE_OUT.toUpperCase());
            PropertyReader.setLogProperties(responseoutAttributes, RESPONSE_OUT);
        } catch (SAXException | ParserConfigurationException | IOException e) {
            log.error("Error while reading Mediator Transaction property file "+ e.getMessage());
        }
    }

    public void readGatewayTransactionProperties() {
        try {
            String configPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + CommonConstant.TRANSACTION_PROPERTY_CONFIGURATION_FILE;
            File fXmlFile = new File(configPath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document document = documentBuilder.parse(fXmlFile);
            document.getDocumentElement().normalize();
            NodeList requestAttributes = document.getElementsByTagName(REQUEST.toUpperCase());
            PropertyReader.setLogProperties(requestAttributes, REQUEST);
            NodeList responseAttributes = document.getElementsByTagName(RESPONSE.toUpperCase());
            PropertyReader.setLogProperties(responseAttributes, RESPONSE);
            NodeList errorAttributes = document.getElementsByTagName(ERROR_RESPONSE.toUpperCase());
            PropertyReader.setLogProperties(errorAttributes, ERROR_RESPONSE);
        } catch (SAXException | ParserConfigurationException | IOException er) {
            log.error("Error while reading Gateway Transaction property file "+ er.getMessage());
        }
    }
}
