package de.ipvs.as.mbp.service.messaging.handler;

/**
 * Message handler that is notified about all messages that are published at the publish-subscribe
 * messaging broker under a topic that matches at least one of the topic filters for which subscriptions were
 * previously created.
 */
public interface PubSubMessageHandler {
    /**
     * Handles a message that was published at the publish-subscribe messaging broker under a topic that
     * matches at least one of the topic filters for which subscriptions were previously created.
     *
     * @param topic   The topic under which the message was published
     * @param message The published message
     */
    void handleMessage(String topic, String message);
}
