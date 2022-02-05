package de.ipvs.as.mbp.service.messaging.handler;

/**
 * Handler for messaging-related exceptions that are thrown during the communication between client and
 * the publish-subscribe messaging broker.
 */
public interface PubSubExceptionHandler {
    /**
     * Handles a messaging-related exception that was thrown during the communication between client and
     * the publish-subscribe messaging broker.
     *
     * @param exception The exception to handle
     */
    void handleException(Exception exception);
}
