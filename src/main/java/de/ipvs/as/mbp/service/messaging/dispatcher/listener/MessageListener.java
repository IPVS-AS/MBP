package de.ipvs.as.mbp.service.messaging.dispatcher.listener;

/**
 * Base interface for callback interfaces that are triggered when a message, which was received by the
 * publish-subscribe-based messaging service, needs to be dispatched to a component of the MBP by the message
 * dispatcher of the messaging service.
 *
 * @param <T> The type of the received message
 */
public interface MessageListener<T> {
    /**
     * Called when a message is received from the messaging broker that was published under a topic that matches
     * the topic filter to which the implementing component subscribed itself.
     *
     * @param message     The received message
     * @param topic       The topic under which the message was published
     * @param topicFilter The topic filter that was used in the corresponding subscription at the message dispatcher
     */
    void onMessageDispatched(T message, String topic, String topicFilter);
}
