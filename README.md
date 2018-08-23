System Requirements
=================

        Java SE Development Kit 1.8


Enable Request ID and Payload Logging
=====================================

Follow below three steps for enabling request ID and payload logging

1) Apply logging-extension-1.0.0-SNAPSHOT.jar to API Manager;
        a) Copy and paste the 'logging-extension-1.0.0-SNAPSHOT.jar' in to deployed api manager lib directory. (Patch source is available under "/wso2telcohub/repository/components/lib".)

                The actual deployment path is as follows;
                wso2telcohub-2.0.0/repository/components/dropins



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


3) Synapse changes for enabling Request ID and Payload Logging;
    a) If the request ID and the payload of a request needs to be logged for a particular API, then the synapse configuration file related to that particular API has to be opened and below two entries have to be added just before the send mediator in the inSequence.

		<property name="message.type" scope="axis2" type="STRING" value="request"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

    b) If the request ID and the payload of the response from backend needs to be logged for a particular API, then the synapse configuration file related to that particular API has to be opened and below two entries have to be added as the first two elements inside the outSequence.
		<property name="message.type" scope="axis2" type="STRING" value="response"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

    c) If the request ID and the payload of the error response from backend needs to be logged for a particular API, then the synapse configuration file related to that particular API has to be opened and below two entries have to be added as the first two elements inside the _cors_request_handler_.xml.
        <property name="message.type" scope="axis2" type="STRING" value="error"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>


(Synapse Configurations related to apis are located at wso2telcohub/repository/deployment/server/synapse-configs/default/api)

Enable Payload body from registry
=================================

1) Log in to the G-Reg Management Console using the following URL and admin/admin credentials:https://<hostname>:9444/carbon/ and
go to _system/governance/apimgt

2) Click on "Add Resource" and select the "Create text contant" in Method

3) Please insert Name: payload.logging.enabled and Content :true and save


Enable Response Time Logging
============================


1) Enable Response Time Logging

	a) If response time needs to be logged then copy WSO2AM--Ext--In.xml and WSO2AM--Ext--Out.xml sequences located in wso2am-2.0.0/repository/deployment/server/synapse-configs/default/sequences and paste those two files to wso2telcohub-2.0.0/repository/deployment/server/synapse-configs/default/sequences directory. This will enable response time logging for all the apis available.
