package de.ipvs.as.mbp.service.messaging.dispatcher;

import de.ipvs.as.mbp.service.messaging.PubSubClient;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.JSONMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.MessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.SubscriptionMessageListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Other components can use the message dispatcher in order to subscribe themselves to certain topic filters and
 * become notified by a callback in case a message was published under a matching topic.
 */
public class MessageDispatcher {
    //Reference to the publish-subscribe-based client that is used
    private PubSubClient pubSubClient;

    //Map (topic filter --> list of subscription) to store subscriptions
    private Map<String, Set<SubscriptionMessageListener>> subscriptions;

    /**
     * Creates and initializes the message dispatcher.
     *
     * @param pubSubClient The publish-subscribe-based client that is used
     */
    public MessageDispatcher(PubSubClient pubSubClient) {
        //Store reference to client
        this.pubSubClient = pubSubClient;

        //Initialize subscription map
        this.subscriptions = new HashMap<>();
    }

    public void subscribe(String topicFilter, MessageListener listener) {
        //Perform subscription
        addSubscription(topicFilter, listener);
    }

    public void subscribeJSON(String topicFilter, JSONMessageListener listener) {
        //Perform subscription
        addSubscription(topicFilter, listener);
    }

    /**
     * Unsubscribes a listener from a given topic filter. The topic filter must be exactly the same as the one
     * that was used in the subscription of the listener. As a result, this method returns whether there are remaining
     * subscriptions for this topc filter.
     *
     * @param topicFilter The topic filter to unsubscribe from
     * @param listener    The listener to unsubscribe
     * @return True, if there are remaining subscriptions for this topic filter; false otherwise
     */
    public boolean unsubscribe(String topicFilter, SubscriptionMessageListener listener) {
        //Check if topic filter is part of the subscription map
        if (!subscriptions.containsKey(topicFilter)) {
            return false;
        }

        //Get subscribers from subscription map
        Set<SubscriptionMessageListener> subscribers = subscriptions.get(topicFilter);

        //Check if listener is part of the subscribers
        if (!subscribers.contains(listener)) {
            return !subscribers.isEmpty();
        }

        //Remove subscription
        subscribers.remove(listener);

        //Check if subscriber set is now empty
        if (subscribers.isEmpty()) {
            //Remove corresponding entry from the subscriptions map
            this.subscriptions.remove(topicFilter);

            //No remaining subscriptions for this topic filter
            return false;
        }

        //There are remaining subscriptions for this topic filter
        return true;
    }

    public void dispatchMessage(String topic, String message) {
        //Iterate over all subscribed topic filters that match the topic
        this.subscriptions
                .keySet().stream().filter(t -> pubSubClient.topicMatchesFilter(topic, t)).forEach(topicFilter -> {
            //Iterate over all subscribers of this topic filter
            subscriptions.get(topicFilter).forEach(listener -> {
                //Check subscription type
                if (listener instanceof JSONMessageListener) {
                    //Convert message to JSON object
                    JSONObject jsonMessage = convertMessageToJSON(message);

                    //Notify subscriber
                    ((JSONMessageListener) listener).onMessagePublished(jsonMessage, topic, topicFilter);
                } else if (listener instanceof MessageListener) {
                    //Notify subscriber
                    ((MessageListener) listener).onMessagePublished(message, topic, topicFilter);
                }
            });
        });
    }

    private JSONObject convertMessageToJSON(String message) {
        try {
            //Try to convert message to JSON object
            return new JSONObject(message);
        } catch (JSONException e) {
            //Failed, return empty JSON object
            return new JSONObject();
        }

    }

    private void addSubscription(String topicFilter, SubscriptionMessageListener listener) {
        //Check whether the topic filter is already registered
        if (subscriptions.containsKey(topicFilter)) {
            //Get subscriber list
            Set<SubscriptionMessageListener> subscribers = subscriptions.get(topicFilter);

            //Check if subscriber is already part of the set
            if (subscribers.contains(listener)) {
                return;
            }

            //Add subscriber to list
            subscribers.add(listener);
        } else {
            //Add subscription to map
            Set<SubscriptionMessageListener> subscribers = new HashSet<>();
            subscribers.add(listener);
            subscriptions.put(topicFilter, subscribers);
        }
    }
}
