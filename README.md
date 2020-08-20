System Requirements
=================

        Java SE Development Kit 1.8


Enable Request ID and Payload Logging
=====================================

With this new Log Manger
Mediator log Handler will log Request-In, Request-Out, Response-In, Response-Out messages

Follow below three steps for enabling request ID and payload logging

1. Apply logging-extension-2.1.0 jar to AM/ESB;
		a) Copy and paste the 'logging-extension-2.1.0.jar' in to deployed api manager/esb lib directory. (Patch source is available under "{AM/ESB Home}/repository/components/lib".)
		b) Apply the logManagerConfig.xml (config/logManagerConfig.xml) file (located in conf folder <APIM/ESB>/repository/conf)
		
2. Enable Log4J properties for the newly installed feature
    
    a) Open the log4j.properties file which is located in <APIM/ESB>/repository/conf folder.
    
	b) Add below entries at the end of the file opened in step (a)

		#custom logger
        log4j.logger.com.wso2telco.mediator.log.handler.SynapseLogHandler=DEBUG
         
        # This works only with notifyEvent mediator
        log4j.category.AXP_ANALYTICS_LOGGER=INFO, AXP_ANALYTICS_APPENDER
        log4j.additivity.AXP_ANALYTICS_LOGGER=false
         
        # Appender config to AXP_ANALYTICS_APPENDER
        log4j.appender.AXP_ANALYTICS_APPENDER=org.apache.log4j.DailyRollingFileAppender
        log4j.appender.AXP_ANALYTICS_APPENDER.File=${carbon.home}/repository/logs/${instance.log}/axp-analytics-logger${instance.log}.log
        log4j.appender.AXP_ANALYTICS_APPENDER.Append=true
        log4j.appender.AXP_ANALYTICS_APPENDER.layout=org.wso2.carbon.utils.logging.TenantAwarePatternLayout
        log4j.appender.AXP_ANALYTICS_APPENDER.layout.ConversionPattern=[%d] %P%5p {%c} - %x %m %n
        log4j.appender.AXP_ANALYTICS_APPENDER.layout.TenantPattern=%U%@%D [%T] [%S]
        log4j.appender.AXP_ANALYTICS_APPENDER.threshold=DEBUG

	c) Save the edited log4j.properties file.
	
3. Synapse changes for enabling Logging (Synapse Configurations files located at <APIM/ESB>/repository/conf/synapse-handlers.xml file);

		<handlers>
             <handler name = "SynapseLogHandler" class="com.wso2telco.mediator.log.handler.SynapseLogHandler"/>
        </handlers>

