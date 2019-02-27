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
    a) If the request ID and the payload of a request needs to be logged for all APIs, then add the following two elements inside the sequence tag of WSO2AM-ExtIn.xml
    
		<property name="message.type" scope="axis2" type="STRING" value="request"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

    b) If the request ID and the payload of the response from backend needs to be logged for All APIs, then add the following two elements inside the sequence tag of WSO2AMExt-Out.xml
		
		<property name="message.type" scope="axis2" type="STRING" value="response"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

    c) If the request ID and the payload of the error response from backend needs to be logged for all APIs, then add the below two entries to _throttle_out_handler_.xml , _auth_failure_handler_.xml and fault.xml
    
	      <property name="message.type" scope="axis2" type="STRING" value="error"/>
        <class name="com.wso2telco.logging.PropertyLogHandler"/>

(Synapse Configurations files located at wso2telcohub/repository/deployment/server/synapse-configs/default/sequences)

Enable Payload body from registry
=================================

1) Log in to the G-Reg Management Console using the following URL and admin/admin credentials:https://<hostname>:9444/carbon/ and
go to _system/governance/apimgt

2) Click on "Add Resource" and select the "Create text contant" in Method

3) Please insert Name: payload.logging.enabled and Content :true and save


Enable Request ID and Payload Logging for ESB
=============================================

Follow below three steps for enabling request ID and payload logging in ESB

1) Apply logging-extension-2.0.0.jar to ESB;
        a) Copy and paste the 'logging-extension-2.0.0.jar' in to deployed ESB lib directory (ESB_HOME/repository/components/lib/)


2) Enable Log4J properties for the newly installed feature
	a) Open the log4j.properties file which is located in ESB_HOME/repository/conf folder
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


3) Synapse changes for enabling Request ID and Payload Logging for ESB;

payment API
-----------

paymentMainSeq (add below under <sequence> line)

	<property expression="get-property('SYSTEM_TIME')" name="REQUEST_TIMESTAMP" scope="default" type="STRING"/>
	<property name="message.type" scope="axis2" type="STRING" value="request"/>
	<class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

paymentOutMainSeq

	<property name="RESPONSE_TIMESTAMP" expression="get-property('SYSTEM_TIME')"/>
	<class name="org.wso2telco.dep.nashornmediator.NashornMediator">
			<property name="script" value="
					var requestTimeStamp = mc.getProperty('REQUEST_TIMESTAMP');
					var responseTimeStamp = mc.getProperty('RESPONSE_TIMESTAMP');
					var responseTime = responseTimeStamp - requestTimeStamp;
					mc.setProperty('RESPONSE_TIME', responseTime);
			"/>
	</class>
	<property name="message.type" scope="axis2" type="STRING" value="response"/>
	<class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

location API
------------

locationSequence (add below under <sequence> line)

    <property expression="get-property('SYSTEM_TIME')"
        name="REQUEST_TIMESTAMP" scope="default" type="STRING" xmlns:ns="http://org.apache.synapse/xsd"/>
    <property name="message.type" scope="axis2" type="STRING" value="request"/>
    <class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

locationSequence (add below Just before <respond/>)

    <property expression="get-property('SYSTEM_TIME')"
        name="RESPONSE_TIMESTAMP" xmlns:ns="http://org.apache.synapse/xsd"/>
    <class name="org.wso2telco.dep.nashornmediator.NashornMediator">
        <axis2ns22:property name="script"
            value="      var requestTimeStamp = mc.getProperty('REQUEST_TIMESTAMP');      var responseTimeStamp = mc.getProperty('RESPONSE_TIMESTAMP');      var responseTime = responseTimeStamp - requestTimeStamp;      mc.setProperty('RESPONSE_TIME', responseTime);    " xmlns:axis2ns22="http://ws.apache.org/ns/synapse"/>
    </class>
    <property name="message.type" scope="axis2" type="STRING" value="response"/>
    <class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

smsmessaging API
----------------

smsMainSeq (add below under <sequence> line)

    <property expression="get-property('SYSTEM_TIME')"
        name="REQUEST_TIMESTAMP" scope="default" type="STRING" xmlns:ns="http://org.apache.synapse/xsd"/>
    <property name="message.type" scope="axis2" type="STRING" value="request"/>
    <class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

sendSmsSeq (add below just before <respond/>)

    <property expression="get-property('SYSTEM_TIME')"
        name="RESPONSE_TIMESTAMP" xmlns:ns="http://org.apache.synapse/xsd"/>
    <class name="org.wso2telco.dep.nashornmediator.NashornMediator">
        <axis2ns22:property name="script"
            value="      var requestTimeStamp = mc.getProperty('REQUEST_TIMESTAMP');      var responseTimeStamp = mc.getProperty('RESPONSE_TIMESTAMP');      var responseTime = responseTimeStamp - requestTimeStamp;      mc.setProperty('RESPONSE_TIME', responseTime);    " xmlns:axis2ns22="http://ws.apache.org/ns/synapse"/>
    </class>
    <property name="message.type" scope="axis2" type="STRING" value="response"/>
    <class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

Add the same of above to get the response log just before <respond/> in each of the below files

smsRetrieveSeq, smsQueryDeliveryStatusSeq, subscribeToSMSDeliveryNotificationsSeq, sendSMSDeliveryNotificationsSeq, smsInboundSubscriptionSeq, smsInboundNotificationHandlerSeq, smsStopSubsDeliveryNotificationSeq, smsStopSubsMessageNotificationSeq

ussd API
----------------

ussdMainSeq (add below under <sequence> line)

    <property expression="get-property('SYSTEM_TIME')"
        name="REQUEST_TIMESTAMP" scope="default" type="STRING" xmlns:ns="http://org.apache.synapse/xsd"/>
    <property name="message.type" scope="axis2" type="STRING" value="request"/>
    <class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

sendUssdSeq (add below just before <send>)

    <property expression="get-property('SYSTEM_TIME')"
        name="RESPONSE_TIMESTAMP" xmlns:ns="http://org.apache.synapse/xsd"/>
    <class name="org.wso2telco.dep.nashornmediator.NashornMediator">
        <axis2ns22:property name="script"
            value="      var requestTimeStamp = mc.getProperty('REQUEST_TIMESTAMP');      var responseTimeStamp = mc.getProperty('RESPONSE_TIMESTAMP');      var responseTime = responseTimeStamp - requestTimeStamp;      mc.setProperty('RESPONSE_TIME', responseTime);    " xmlns:axis2ns22="http://ws.apache.org/ns/synapse"/>
    </class>
    <property name="message.type" scope="axis2" type="STRING" value="response"/>
    <class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>

Add the same of above to get the response log just before <respond/> in each of the below files

ussdInboundSeq, ussdMOSubscribeSeq, ussdMOStopSubscriptionSeq

Error logging
-------------

commonFault sequence (add below just before <send/>)

	<property name="message.type" scope="axis2" type="STRING" value="error"/>
	<class name="com.wso2telco.logging.PropertyLogHandlerForEsb"/>
