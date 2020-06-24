package com.wso2telco.mediator.log.handler;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.logging.Log;
import org.apache.log4j.MDC;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.MediatorLog;
import org.apache.synapse.transport.passthru.PassThroughConstants;

import java.util.Map;
import java.util.UUID;

public class LogHandlerUtil {

    public static final String TRACKING_ID = "RequestId";
    //Key value to hold "to" address of the service.
    public static final String TRACKING_TO = "To";
    // Key value to hold API name with version
    public static final String TRACKING_API = "api.ut.api_version";
     // Key value to hold HTTP method
    public static final String TRACKING_HTTP_METHOD = "api.ut.HTTP_METHOD";
    // Key value to hold "to" address of the service.
    public static final String TRACKING_MESSAGE_ID = "MessageID";

    /*
     * Sets the required parameters on log4j thread local to enable expected
     * logging parameters.
     *
     * @param context synapse message context.
     * @param log synapse logging.
     */
    public static void setLogContext(MessageContext context, SynapseLog log) {

        //Store the easier on to log4j thread local first
        if (context.getTo() != null)
            MDC.put(TRACKING_TO, context.getTo().getAddress());
        if (context.getProperty(TRACKING_API) != null)
            MDC.put(TRACKING_API, context.getProperty(TRACKING_API));
        if (context.getProperty(TRACKING_HTTP_METHOD) != null)
            MDC.put(TRACKING_HTTP_METHOD,
                    context.getProperty(TRACKING_HTTP_METHOD));
        if (context.getMessageID() != null)
            MDC.put(TRACKING_ID, context.getMessageID());

        try {
                //Check the tracking id in the message context
                String trackingId = generateTrackingId(context);
                //Put the header value on log4j thread local
                MDC.put(TRACKING_ID, trackingId);
            } catch (Exception e) {
                //Do nothing here
                if (log != null) {
                    log.auditWarn("Unable to set the logging context due to " + e);
                }
            }
       // }
    }

    /*
     * Calculates the tracking id based on message context. First looks in
     * message context, else looks in HTTP headers, else generates a new UUID.
     * In case the tracking id is generated, it will be set to HTTP header and
     * synapse context.
     *
     * @param context synapse context.
     * @return tracking id string.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String generateTrackingId(MessageContext context) {
        String trackingId;
        //Check the tracking id in the message context
        String trackingMessageId = (String) context.getMessageID();
        if (trackingMessageId == null) {
           trackingId = UUID.randomUUID().toString();
           context.setProperty(TRACKING_MESSAGE_ID, trackingId);
        } else context.setProperty(TRACKING_MESSAGE_ID, trackingMessageId);
        return trackingMessageId;
    }

    /*
     * Return the HTTP header map reference from synapse context.
     *
     * @param context synapse message context.
     * @return reference to HTTP header map reference.
     */
    @SuppressWarnings("rawtypes")
    public static Map getHTTPHeaders(MessageContext context) {

        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) context).getAxis2MessageContext();

        return (Map) axis2MessageCtx.getProperty(
                org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
    }

      //Clears the thread local values.

    public static void clearLogContext() {

        MDC.remove(TRACKING_TO);
        MDC.remove(TRACKING_API);
        MDC.remove(TRACKING_HTTP_METHOD);
        MDC.remove(TRACKING_MESSAGE_ID);
        MDC.remove(TRACKING_ID);
    }

    /*
     * Get a SynapseLog instance appropriate for the given context.
     *
     * @param synCtx the current message context
     * @return MediatorLog instance - an implementation of the SynapseLog
     */
    public static SynapseLog getLog(MessageContext synCtx, Log log) {
        return new MediatorLog(log, false, synCtx);
    }

    /*
     * Returns the HTTP status code with appended description.
     *
     * @param synCtx the current message context
     * @return HTTP status description.
     */
    public static String getHTTPStatusMessage(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        StringBuilder msg = new StringBuilder();

        Object o = axis2MessageCtx.getProperty(PassThroughConstants.HTTP_SC);

        if (o instanceof Integer) {

            //Set the status code or address
            Integer statusCode =
                    (Integer) axis2MessageCtx.getProperty(PassThroughConstants.HTTP_SC);
            if (statusCode != null) {

                msg.append(statusCode);
                msg.append(" ");
                msg.append(HttpStatus.getStatusText(statusCode));
            }
        } else if (o instanceof String) {
            msg.append(o);
        }

        return msg.toString();
    }

    /*
     * Returns the HTTP method associated with the context.
     *
     * @param synCtx the current message context
     * @return HTTP method name.
     */
    public static String getHTTPMethod(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        return (String) axis2MessageCtx.getProperty("HTTP_METHOD");
    }

    /*
     * Returns the HTTP method associated with the context.
     *
     * @param synCtx the current message context
     * @return HTTP method name.
     */
    public static String getToHTTPAddress(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        EndpointReference to = axis2MessageCtx.getTo();

        if (to != null)
            return to.getAddress();

        return "";
    }

    /*
     * Returns the HTTP method associated with the context.
     *
     * @param synCtx the current message context
     * @return HTTP method name.
     */
    public static String getReplyToHTTPAddress(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        EndpointReference replyTo = axis2MessageCtx.getReplyTo();

        if (replyTo != null)
            return replyTo.getAddress();

        return "";
    }
}
