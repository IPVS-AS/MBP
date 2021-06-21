package de.ipvs.as.mbp.service.messaging.dispatcher;

import de.ipvs.as.mbp.service.messaging.dispatcher.listener.JSONMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.MessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.SubscriptionMessageListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Other components can use the message dispatcher in order to subscribe themselves to certain topic filters and
 * become notified by a callback in case a message was published under a matching topic.
 */
public class MessageDispatcher {
    //Map (topic filter --> list of subscription) to store subscriptions
    private Map<String, Set<SubscriptionMessageListener>> subscriptions;

    /**
     * Creates and initializes the message dispatcher.
     */
    public MessageDispatcher() {
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
