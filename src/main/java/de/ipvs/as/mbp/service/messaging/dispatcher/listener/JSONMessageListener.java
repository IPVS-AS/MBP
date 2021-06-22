package de.ipvs.as.mbp.service.messaging.dispatcher.listener;

import org.json.JSONObject;

/**
 * This interface provides a callback method that is triggered when a JSON message is delivered by the messaging broker
 * that was published under a topic that matches the topic filter to which the component implementing this interface
 * subscribed itself to at the message dispatcher of the messaging service.
 */
public interface JSONMessageListener extends MessageListener {
    /**
     * Called when a JSON message is received from the messaging broker that was published under a topic that matches
     * the topic filter to which the implementing component subscribed itself.
     *
     * @param message     The received message
     * @param topic       The topic under which the message was published
     * @param topicFilter The topic filter that was used in the corresponding subscription at the message dispatcher
     */
    void onMessageDispatched(JSONObject message, String topic, String topicFilter);
}
