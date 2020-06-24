System Requirements
=================

        Java SE Development Kit 1.8


Enable Request ID and Payload Logging
=====================================

Follow below three steps for enabling request ID and payload logging

1) Apply logging-extension-2.0.2.jar to API Manager;
		a) Copy and paste the 'logging-extension-2.0.2.jar' in to deployed api manager lib directory. (Patch source is available under "/wso2telcohub/repository/components/lib".)
		b) Apply the logManagerConfig.xml (config/logManagerConfig.xml) file (located in conf folder to Gateway node <APIM>repository/conf)
		


2) Enable Log4J properties for the newly installed feature
	a) Open the log4j.properties file which is located in wso2telcohub/repository/conf folder
	b) Add below entries at the end of the file opened in step (a)

		log4j.logger.com.wso2telco.logging=DEBUG

		# The request response logger failed events when trying to publish events
        # This works only with notifyEvent mediator
        log4j.category.REQUEST_RESPONSE_LOGGER=INFO, REQUEST_RESPONSE_APPENDER
        log4j.additivity.REQUEST_RESPONSE_LOGGER=false

        # Appender config to REQUEST_RESPONSE_APPENDER
        log4j.appender.REQUEST_RESPONSE_APPENDER=org.apache.log4j.DailyRollingFileAppender
        log4j.appender.REQUEST_RESPONSE_APPENDER.File=${carbon.home}/repository/logs/${instance.log}/request-response-logger${instance.log}.log
        log4j.appender.REQUEST_RESPONSE_APPENDER.Append=true
        log4j.appender.REQUEST_RESPONSE_APPENDER.layout=org.wso2.carbon.utils.logging.TenantAwarePatternLayout
        log4j.appender.REQUEST_RESPONSE_APPENDER.layout.ConversionPattern=[%d] %P%5p {%c} - %x %m %n
        log4j.appender.REQUEST_RESPONSE_APPENDER.layout.TenantPattern=%U%@%D [%T] [%S]
        log4j.appender.REQUEST_RESPONSE_APPENDER.threshold=DEBUG

	c) Save the edited log4j.properties file.


3) Synapse changes for enabling Request ID and Payload Logging (Synapse Configurations files located at wso2telcohub/repository/deployment/server/synapse-configs/default/sequences);
    a) If the request ID and the payload of a request needs to be logged for all APIs, then add the following two elements inside the sequence tag of WSO2AM-ExtIn.xml
    
		<property name="message.type" scope="axis2" type="STRING" value="request"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

    b) If the request ID and the payload of the response from backend needs to be logged for All APIs, then add the following two elements inside the sequence tag of WSO2AMExt-Out.xml
		
		<property name="message.type" scope="axis2" type="STRING" value="response"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

    c) If the request ID and the payload of the error response from backend needs to be logged for all APIs, then add the below two entries to _throttle_out_handler_.xml , _auth_failure_handler_.xml and fault.xml
    
	      <property name="message.type" scope="axis2" type="STRING" value="error"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>



Enable Payload body from registry
=================================

1) Log in to the G-Reg Management Console using the following URL and admin/admin credentials:https://<hostname>:9444/carbon/ and
go to _system/governance/apimgt

2) Click on "Add Resource" and select the "Create text contant" in Method

3) Please insert Name: payload.logging.enabled and Content :true and save


Enable Request,Response logging in ESB,EI
=========================================
(com.wso2telco.mediator.log.handler.SynapseLogHandler)


com.wso2telco.mediator.log.handler is handler which can used to log API transaction details in ESB/EI.

Please follow the steps below to configure the given jar in your environment.

1) build the repository and go to target folder
2) Copy Jar into <ESB_HOME>/repository/components/lib
3) Add the following configurations to <ESB_HOME>/repository/conf/synapse-handlers.xml file
```
<handlers>
     <handler name = "SynapseLogHandler" class="com.wso2telco.mediator.log.handler.SynapseLogHandler"/>
</handlers>

```
4) Add the following configurations to <ESB_HOME>/repository/conf/log4j.properties
```
log4j.logger.com.wso2telco.mediator.log.handler.SynapseLogHandler=DEBUG
```
5) Start the server and create an API.
6) When you invoke the API, you will be able to see the logs successfully.



