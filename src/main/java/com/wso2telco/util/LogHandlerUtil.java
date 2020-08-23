package com.wso2telco.util;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.log4j.MDC;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.wso2telco.util.CommonConstant.*;

public class LogHandlerUtil {
    public static org.apache.axis2.context.MessageContext axis2MessageContext;
    public static Map<String, Object> headerMap;

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
    public static void generateTrackingId(MessageContext messageContext, String amMapping, String esbMapping) throws IOException, XMLStreamException {
        String trackingId;
        RelayUtils.buildMessage(((Axis2MessageContext) messageContext).getAxis2MessageContext());
        axis2MessageContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        headerMap = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        //Check the tracking id in the message context
        String trackingMessageId = null;
        try {
            if(null!=headerMap.get(amMapping))
                trackingMessageId = headerMap.get(amMapping).toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (trackingMessageId == null) {
                trackingMessageId = (String) messageContext.getMessageID();
                if (trackingMessageId == null) {
                    trackingMessageId = UUID.randomUUID().toString();
                    messageContext.setProperty(esbMapping, trackingMessageId);
                }
                messageContext.setProperty(esbMapping, trackingMessageId);
            } else messageContext.setProperty(esbMapping, trackingMessageId);
        }

    }

    public static String generateTrackingId(MessageContext context) {
        String trackingId;
        axis2MessageContext = ((Axis2MessageContext) context).getAxis2MessageContext();
        headerMap = (Map<String, Object>) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        //Check the tracking id in the message context
        String trackingMessageId = null;
        try {
            trackingMessageId = headerMap.get(TRACKING_MESSAGE_ID).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (trackingMessageId == null) {
            trackingMessageId = (String) context.getMessageID();
            if (trackingMessageId == null) {
                trackingMessageId = UUID.randomUUID().toString();
                context.setProperty(TRACKING_MESSAGE_ID, trackingMessageId);
            }
        } else context.setProperty(TRACKING_MESSAGE_ID, trackingMessageId);
        return trackingMessageId;
    }

    /*
     * Return the HTTP header map reference from synapse context.
     *
     * @param context synapse message context.
     * @return reference to HTTP header map reference.
     */
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
     * Returns the HTTP status code with appended description.
     *
     * @param messageContext the current message context
     * @return HTTP status description.
     */
    public static String getHTTPStatusMessage(MessageContext messageContext) {

        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();

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
     * @param messageContext the current message context
     * @return HTTP method name.
     */
    public static String getHTTPMethod(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        return (String) axis2MessageCtx.getProperty(HTTP_METHOD);
    }

    /*
     * Returns the HTTP method associated with the context.
     *
     * @param messageContext the current message context
     * @return HTTP method name.
     */
    public static String getToHTTPAddress(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        EndpointReference to = axis2MessageCtx.getTo();
        if (to != null)
            return to.getAddress();
        return "";
    }

    /*
     * Returns the HTTP method associated with the context.
     *
     * @param messageContext the current message context
     * @return HTTP method name.
     */
    public static String getReplyToHTTPAddress(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MessageCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        EndpointReference replyTo = axis2MessageCtx.getReplyTo();
        if (replyTo != null)
            return replyTo.getAddress();
        return "";
    }


}
