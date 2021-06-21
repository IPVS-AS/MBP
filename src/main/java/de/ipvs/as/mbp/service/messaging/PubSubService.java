package de.ipvs.as.mbp.service.messaging;

import de.ipvs.as.mbp.service.messaging.dispatcher.MessageDispatcher;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.JSONMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.MessageListener;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * This service offers technology-agnostic messaging functions for interacting with publish-subscribe-based middleware
 * that is connected to the MBP.
 */
@Service
public class PubSubService {
    //Autowired
    private PubSubClient pubSubClient;

    private MessageDispatcher messageDispatcher;

    /**
     * Creates and initializes the service for a given client that enables publish-subscribe-based messaging.
     *
     * @param pubSubClient The for client for publish-subscribe (auto-wired)
     */
    @Autowired
    public PubSubService(PubSubClient pubSubClient) {
        this.pubSubClient = pubSubClient;
        this.messageDispatcher = new MessageDispatcher();
    }


    public void publish(String topic, JSONObject jsonObject) {
        //Transform provided JSON message to string and publish it
        publish(topic, jsonObject.toString());
    }

    public void publish(String topic, String message) {
        //TODO
    }

    public void publish(Collection<String> topics, JSONObject jsonObject) {
        //Transform provided JSON message to string and publish it
        publish(topics, jsonObject.toString());
    }

    public void publish(Collection<String> topics, String message) {
        //Publish the message under each provided topic individually
        topics.forEach(t -> publish(t, message));
    }

    public void subscribe(String topicFilter, MessageListener listener) {
        //Perform subscription
        addSubscription(topicFilter, listener);
    }

    public void subscribeJSON(String topicFilter, JSONMessageListener listener) {
        //Perform subscription
        addSubscription(topicFilter, listener);
    }
}
