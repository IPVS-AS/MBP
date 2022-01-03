package de.ipvs.as.mbp.service.messaging.dispatcher.listener;

import org.json.JSONObject;

/**
 * This interface provides a callback method that is triggered when a JSON message is delivered by the messaging broker
 * that was published under a topic that matches the topic filter to which the component implementing this interface
 * subscribed itself to at the message dispatcher of the messaging service.
 */
public interface JSONMessageListener extends MessageListener<JSONObject> {

}
