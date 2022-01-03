package de.ipvs.as.mbp.service.messaging.dispatcher;

import de.ipvs.as.mbp.service.messaging.PubSubClient;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.DomainMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.JSONMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.MessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Other components can subscribe themselves to the message dispatcher with a given topic filter and will
 * subsequently be notified when an incoming message is forwarded to the message dispatcher that was published
 * under a topic matching this topic filter. The publish-subscribe-based messaging service of the MBP uses
 * an instance of this dispatcher in order to distribute messages that are delivered by the messaging broker
 * to interested components of the MBP.
 */
public class MessageDispatcher {
    //Reference to the publish-subscribe-based client that is used
    private final PubSubClient pubSubClient;

    //Map (topic filter --> list of subscriptions) to store subscriptions
    private final Map<String, Set<MessageListener<?>>> subscriptionMap;

    /**
     * Creates and initializes the message dispatcher.
     *
     * @param pubSubClient The publish-subscribe-based client that is used
     */
    public MessageDispatcher(PubSubClient pubSubClient) {
        //Store reference to client
        this.pubSubClient = pubSubClient;

        //Initialize subscription map
        this.subscriptionMap = new ConcurrentHashMap<>();
    }

    /**
     * Subscribes a given message listener to a given topic filter at the dispatcher, such that incoming messages
     * will be dispatched to the listener if the topic of the message matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to dispatch matching messages to
     */
    public synchronized void subscribe(String topicFilter, StringMessageListener listener) {
        //Perform subscription
        performSubscription(topicFilter, listener);
    }

    /**
     * Subscribes a given JSON message listener to a given topic filter at the dispatcher, such that incoming
     * JSON messages will be dispatched to the listener if the topic of the message matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to dispatch matching messages to
     */
    public synchronized void subscribeJSON(String topicFilter, JSONMessageListener listener) {
        //Perform subscription
        performSubscription(topicFilter, listener);
    }

    /**
     * Subscribes a given domain message listener to a given topic filter at the dispatcher, such that incoming
     * domain messages will be dispatched to the listener if the topic of the message matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to dispatch matching messages to
     */
    public synchronized void subscribeDomain(String topicFilter, DomainMessageListener<?> listener) {
        //Perform subscription
        performSubscription(topicFilter, listener);
    }

    /**
     * Unsubscribes a listener from a given topic filter at the message dispatcher. The topic filter must be
     * exactly the same as the one that was used in the subscription of the listener. As a result, this method
     * returns whether there are still remaining subscriptions for this topic filter.
     *
     * @param topicFilter The topic filter to unsubscribe the listener from
     * @param listener    The listener to unsubscribe
     * @return True, if there are remaining subscriptions for this topic filter; false otherwise
     */
    public synchronized boolean unsubscribe(String topicFilter, MessageListener<?> listener) {
        //Check if the subscription map contains the topic filter
        if (!this.subscriptionMap.containsKey(topicFilter)) {
            return false;
        }

        //Get subscriptions for this topic filter from map
        Set<MessageListener<?>> subscriptions = this.subscriptionMap.get(topicFilter);

        //Check if listener is already subscribed
        if (!subscriptions.contains(listener)) {
            return !subscriptions.isEmpty();
        }

        //Remove subscription
        subscriptions.remove(listener);

        //Check if there are remaining subscriptions for this topic filter
        if (subscriptions.isEmpty()) {
            //Remove corresponding entry from the subscriptions map
            this.subscriptionMap.remove(topicFilter);

            //No remaining subscriptions for this topic filter
            return false;
        }

        //There are remaining subscriptions for this topic filter
        return true;
    }

    /**
     * Dispatches a message, given as string, to the listeners that subscribed themselves at the message dispatcher
     * to at least one topic filter that matches the topic of the message.
     *
     * @param topic   The topic of the message to dispatch
     * @param message The body of the message to dispatch
     */
    public synchronized void dispatchMessage(String topic, String message) {
        //Create map (listener --> topic filter) for all subscribers that need to be notified
        Map<MessageListener<?>, String> subscribers = new HashMap<>();

        //Determine affected subscribers and their subscribed topic filters and add them to the map
        this.subscriptionMap.keySet().stream() //Stream through all subscribed topic filters
                .filter(t -> pubSubClient.topicMatchesFilter(topic, t)) //Filter for matches with the message topic
                .forEach(tf -> this.subscriptionMap.get(tf).forEach(s -> subscribers.put(s, tf)));

        //Stream through all affected subscribers and notify them
        subscribers.forEach((listener, topicFilter) -> {
            //Check listener type
            if (listener instanceof DomainMessageListener) {
                //Notify listener
                ((DomainMessageListener<?>) listener).onMessageDispatched(message, topic, topicFilter);
            } else if (listener instanceof StringMessageListener) {
                //Notify listener
                ((StringMessageListener) listener).onMessageDispatched(message, topic, topicFilter);
            } else if (listener instanceof JSONMessageListener) {
                //Convert message to JSON object
                JSONObject jsonMessage = transformMessageToJSON(message);
                //Notify listener
                ((JSONMessageListener) listener).onMessageDispatched(jsonMessage, topic, topicFilter);
            }
        });
    }

    /**
     * Tries to transform a message, given as string, to a JSON object. If this is not possible, an empty JSON object
     * will be returned.
     *
     * @param message The message to transform
     * @return The resulting JSONObject or an empty JSONObject of the transformation failed
     */
    private JSONObject transformMessageToJSON(String message) {
        try {
            //Try to convert message to JSON object
            return new JSONObject(message);
        } catch (JSONException e) {
            //Failed, return empty JSON object
            return new JSONObject();
        }

    }

    /**
     * Subscribes a given message listener to a given topic filter at the dispatcher, such that incoming messages
     * will be dispatched to the given listener if the topic of the message matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to dispatch matching messages to
     */
    private synchronized void performSubscription(String topicFilter, MessageListener<?> listener) {
        //Check whether the topic filter is already registered
        if (subscriptionMap.containsKey(topicFilter)) {
            //Get all subscriptions for this topic filter
            Set<MessageListener<?>> subscriptions = subscriptionMap.get(topicFilter);

            //Check if listener is already subscribed
            if (subscriptions.contains(listener)) {
                return;
            }

            //Add listener to the subscriptions
            subscriptions.add(listener);
        } else {
            //Create new set of subscriptions for this topic filter
            Set<MessageListener<?>> listeners = new HashSet<>();
            listeners.add(listener);
            subscriptionMap.put(topicFilter, listeners);
        }
    }
}
