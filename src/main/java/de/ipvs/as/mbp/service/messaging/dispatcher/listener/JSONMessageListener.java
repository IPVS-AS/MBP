package de.ipvs.as.mbp.service.messaging.dispatcher.listener;

import org.json.JSONObject;

/**
 * Interface with callback that is triggered when a JSON message is published under a topic to which the implementing
 * component subscribed itself.
 */
public interface JSONMessageListener extends SubscriptionMessageListener {
    /**
     * Called when a JSON message is published on a topic to which the implementing component subscribed itself.
     *
     * @param message     The published message as JSON object
     * @param topic       The topic under which the message was published
     * @param topicFilter The topic filter that was used in the subscription
     */
    void onMessagePublished(JSONObject message, String topic, String topicFilter);
}
