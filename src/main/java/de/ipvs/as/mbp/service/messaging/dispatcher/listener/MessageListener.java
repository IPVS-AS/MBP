package de.ipvs.as.mbp.service.messaging.dispatcher.listener;

/**
 * Interface with callback that is triggered when a message is published under a topic to which the implementing
 * component subscribed itself.
 */
public interface MessageListener extends SubscriptionMessageListener {
    /**
     * Called when a message is published on a topic to which the implementing component subscribed itself.
     *
     * @param message     The published message
     * @param topic       The topic under which the message was published
     * @param topicFilter The topic filter that was used in the subscription
     */
    void onMessagePublished(String message, String topic, String topicFilter);
}
