package de.ipvs.as.mbp.service.messaging;

import de.ipvs.as.mbp.domain.settings.BrokerLocation;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.messaging.dispatcher.MessageDispatcher;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.DomainMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.JSONMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.MessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;
import de.ipvs.as.mbp.service.messaging.topics.ReturnTopicGenerator;
import de.ipvs.as.mbp.service.settings.SettingsService;
import de.ipvs.as.mbp.util.Json;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * This service offers technology-agnostic messaging functions for interacting with a messaging client that connects
 * itself to an external publish-subscribe-based messaging broker.
 */
@Service
@EnableScheduling
@PropertySource(value = "classpath:application.properties")
public class PubSubService {
    //Delay between re-connect attempts
    private static final int RECONNECT_DELAY = 10 * 1000;

    //Auto-wired components
    private final PubSubClient pubSubClient;
    private final ReturnTopicGenerator returnTopicGenerator;
    private final SettingsService settingsService;

    //Dispatcher for incoming messages
    private final MessageDispatcher messageDispatcher;

    //Thread pool for re-connects on connection los
    private final ScheduledExecutorService threadPool = Executors.newSingleThreadScheduledExecutor();

    //Scheduled future for re-connect attempts
    private ScheduledFuture<?> reconnectAttempt;

    //Remembers all topics that are currently subscribed
    private final Set<String> subscribedTopicFilters;

    //Stores the current OAuth2 access token
    private String oauthAccessToken;

    /*
    Inject OAuth2-related settings from the application.properties file.
     */
    @Value("${security.user.name}")
    private String httpUser;

    @Value("${security.user.password}")
    private String httpPassword;

    @Value("${security.oauth2.client.access-token-uri}")
    private String oauth2TokenUri;

    @Value("${security.oauth2.client.grant-type}")
    private String oauth2GrantType;

    @Value("${security.oauth2.client.client-id}")
    private String oauth2ClientId;

    /**
     * Creates and initializes the service for a given {@link PubSubClient} that enables publish-subscribe-based
     * messaging, a {@link ReturnTopicGenerator} that is able to generate return topics and a
     * {@link SettingsService} that manages application settings.
     *
     * @param pubSubClient         The messaging client to use (auto-wired)
     * @param returnTopicGenerator The return topic generator to use (auto-wired)
     * @param settingsService      The settings service (auto-wired)
     */
    @Autowired
    public PubSubService(PubSubClient pubSubClient, ReturnTopicGenerator returnTopicGenerator, SettingsService settingsService) {
        //Store references to components
        this.pubSubClient = pubSubClient;
        this.returnTopicGenerator = returnTopicGenerator;
        this.settingsService = settingsService;

        //Initialize sub-components and data structures
        this.messageDispatcher = new MessageDispatcher(pubSubClient);
        this.subscribedTopicFilters = new HashSet<>();

        //Set a message handler that delegates incoming messages to the dispatcher
        pubSubClient.setMessageHandler(messageDispatcher::dispatchMessage);

        //Provide the responsible method of this service as connection loss handler
        pubSubClient.setConnectionLossHandler(this::handleConnectionLoss);

        //Let the client connect
        connectClient();
    }


    /**
     * Publishes a message, given as string, under a given topic at the messaging broker.
     *
     * @param topic   The topic under which the string message is supposed to be published
     * @param message The message to publish
     */
    public void publish(String topic, String message) {
        //Publish message via the client
        pubSubClient.publish(topic, message);
    }

    /**
     * Publishes a message, given as JSON object, under a given topic at the messaging broker.
     *
     * @param topic      The topic under which the JSON message is supposed to be published
     * @param jsonObject The message to publish
     */
    public void publish(String topic, JSONObject jsonObject) {
        //Transform provided JSON message to string and publish it
        publish(topic, jsonObject.toString());
    }

    /**
     * Publishes a message, given as {@link DomainMessage} object, under a given topic at the
     * messaging broker.
     *
     * @param topic   The topic under which the domain message is supposed to be published
     * @param message The message to publish
     */
    public void publish(String topic, DomainMessage<?> message) {
        //Update timestamp of message
        message.updateTimestamp();

        //Transform the message to JSON
        String jsonString = transformMessageToJSON(message);

        //Publish the message
        publish(topic, jsonString);
    }

    /**
     * Publishes a message, given as string, under several given topics at the messaging broker.
     *
     * @param topics  Collection of topics under which the string message is supposed to be published
     * @param message The message to publish
     */
    public void publish(Collection<String> topics, String message) {
        //Publish the message under each provided topic individually
        topics.forEach(t -> publish(t, message));
    }

    /**
     * Publishes a message, given as JSON object, under several given topics at the messaging broker.
     *
     * @param topics     Collection of topics under which the JSON message is supposed to be published
     * @param jsonObject The message to publish
     */
    public void publish(Collection<String> topics, JSONObject jsonObject) {
        //Transform provided JSON message to string and publish it
        publish(topics, jsonObject.toString());
    }

    /**
     * Publishes a message, given as {@link DomainMessage} object under several given topics at the messaging broker.
     *
     * @param topics  Collection of topics under which the domain message is supposed to be published
     * @param message The message to publish
     */
    public void publish(Collection<String> topics, DomainMessage<?> message) {
        //Update timestamp of message
        message.updateTimestamp();

        //Transform the message to JSON
        String jsonString = transformMessageToJSON(message);

        //Publish the message
        publish(topics, jsonString);
    }

    /**
     * Subscribes a given message listener to a given topic filter at the messaging broker, such that the listener
     * is notified when a message is published at the broker under a topic that matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to call in case a matching message is published at the broker
     */
    public void subscribe(String topicFilter, StringMessageListener listener) {
        //Add subscription
        addSubscription(topicFilter);

        //Create corresponding subscription at the dispatcher
        this.messageDispatcher.subscribe(topicFilter, listener);
    }

    /**
     * Subscribes a given JSON message listener to a given topic filter at the messaging broker, such that the listener
     * is notified when a message is published at the broker under a topic that matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to call in case a matching message is published at the broker
     */
    public void subscribeJSON(String topicFilter, JSONMessageListener listener) {
        //Add subscription
        addSubscription(topicFilter);

        //Create corresponding subscription at the dispatcher
        this.messageDispatcher.subscribeJSON(topicFilter, listener);
    }

    /**
     * Subscribes a given domain message listener to a given topic filter at the messaging broker, such that the listener
     * is notified when a message is published at the broker under a topic that matches the topic filter.
     *
     * @param topicFilter The topic filter to subscribe to
     * @param listener    The listener to call in case a matching message is published at the broker
     */
    public void subscribeDomain(String topicFilter, DomainMessageListener<?> listener) {
        //Add subscription
        addSubscription(topicFilter);

        //Create corresponding subscription at the dispatcher
        this.messageDispatcher.subscribeDomain(topicFilter, listener);
    }

    /**
     * Subscribes a given message listener to several given topic filters at the messaging broker, such that the
     * listener is notified when a message is published at the broker under a topic that matches at least one
     * of the topic filters.
     *
     * @param topicFilters The topic filters to subscribe to
     * @param listener     The listener to call in case a matching message is published at the broker
     */
    public void subscribe(List<String> topicFilters, StringMessageListener listener) {
        //Create one subscription for each topic filter
        topicFilters.forEach(t -> subscribe(t, listener));
    }

    /**
     * Subscribes a given JSON message listener to several given topic filters at the messaging broker, such that the
     * listener is notified when a message is published at the broker under a topic that matches at least one
     * of the topic filters.
     *
     * @param topicFilters The topic filters to subscribe to
     * @param listener     The listener to call in case a matching message is published at the broker
     */
    public void subscribeJSON(List<String> topicFilters, JSONMessageListener listener) {
        //Create one subscription for each topic filter
        topicFilters.forEach(t -> subscribeJSON(t, listener));
    }

    /**
     * Subscribes a given domain message listener to several given topic filters at the messaging broker, such that the
     * listener is notified when a message is published at the broker under a topic that matches at least one
     * of the topic filters.
     *
     * @param topicFilters The topic filters to subscribe to
     * @param listener     The listener to call in case a matching message is published at the broker
     */
    public void subscribeDomain(List<String> topicFilters, DomainMessageListener<?> listener) {
        //Create one subscription for each topic filter
        topicFilters.forEach(t -> subscribeDomain(t, listener));
    }

    /**
     * Unsubscribes a given listener from a given topic filter at the messaging broker. This only has an effect
     * if the listener previously created an subscription at the messaging broker for exactly the same topic filter.
     *
     * @param topicFilter The topic filter to unsubscribe the listener from
     * @param listener    The listener to unsubscribe
     */
    public void unsubscribe(String topicFilter, MessageListener<?> listener) {
        //Unsubscribe from dispatcher
        boolean remainingSubscriptions = this.messageDispatcher.unsubscribe(topicFilter, listener);

        //Check if no subscriptions remain for this topic filter
        if (!remainingSubscriptions) {
            //Remove topic filter from set of subscribed topic filters
            this.subscribedTopicFilters.remove(topicFilter);

            //Unsubscribe topic at message broker
            this.pubSubClient.unsubscribe(topicFilter);
        }
    }

    /**
     * Creates and returns a new {@link ScatterGatherRequestBuilder} including all its dependencies which subsequently
     * can be used to construct scatter gather requests in a step by step manner.
     *
     * @return The created {@link ScatterGatherRequestBuilder}
     */
    public ScatterGatherRequestBuilder buildScatterGatherRequest() {
        //Create new request builder and return it
        return new ScatterGatherRequestBuilder(this, this.returnTopicGenerator);
    }

    /**
     * Creates a return topic from a given category name.
     *
     * @param category The category name to use
     * @return The resulting return topic
     */
    public String generateReturnTopic(String category) {
        //Generate the topic
        return returnTopicGenerator.create(category);
    }

    /**
     * Creates a return topic from a given {@link User} and category name.
     *
     * @param user     The user to use
     * @param category The category name to use
     * @return The resulting return topic
     */
    public String generateReturnTopic(User user, String category) {
        //Generate the topic
        return returnTopicGenerator.create(user, category);
    }

    /**
     * Gracefully disconnects from the messaging broker if a connection exists and
     * re-establishes the connection by using the broker settings that are returned by the settings service.
     */
    public void reconnect() {
        //Execute re-connect
        connectClient();
    }

    /**
     * Gracefully disconnects from the messaging broker if a connection exists and
     * re-establishes the connection by using a given broker location, broker address and broker port.
     *
     * @param brokerLocation The broker location to use
     * @param brokerAddress  The broker address to use
     * @param brokerPort     The broker port to use
     */
    public void reconnect(BrokerLocation brokerLocation, String brokerAddress, int brokerPort) {
        //Execute re-connect
        connectClient(brokerLocation, brokerAddress, brokerPort);
    }

    /**
     * Checks and returns whether the client is currently connected to the messaging broker.
     *
     * @return True, if the client is connected to the messaging broker; false otherwise.
     */
    public boolean isConnected() {
        return pubSubClient.isConnected();
    }

    /**
     * Lets the messaging client establish a connection to the messaging broker by using the broker settings
     * as provided by the settings service.
     */
    private void connectClient() {
        //Retrieve current settings from the settings service
        Settings settings = settingsService.getSettings();

        //Get broker location and address
        BrokerLocation brokerLocation = settings.getBrokerLocation();
        String brokerAddress = settings.getBrokerIPAddress();
        int brokerPort = settings.getBrokerPort();

        //Establish the connection
        connectClient(brokerLocation, brokerAddress, brokerPort);
    }

    /**
     * Lets the messaging client establish a connection to the messaging broker by using the giving broker location,
     * broker address and broker port.
     *
     * @param brokerLocation THe broker location to use
     * @param brokerAddress  The broker address to use
     * @param brokerPort     The broker port to use
     */
    private void connectClient(BrokerLocation brokerLocation, String brokerAddress, int brokerPort) {
        //Check whether broker is local
        if (brokerLocation.equals(BrokerLocation.LOCAL) || brokerLocation.equals(BrokerLocation.LOCAL_SECURE)) {
            //Override broker address
            brokerAddress = "localhost";
        }

        //Check whether a secure connection is desired
        if (brokerLocation.equals(BrokerLocation.LOCAL_SECURE) || brokerLocation.equals(BrokerLocation.REMOTE_SECURE)) {
            //Request new OAuth2 token
            requestOAuth2Token();

            //Establish a secure connection
            pubSubClient.connectSecure(brokerAddress, brokerPort, this.oauthAccessToken, "any");
        } else {
            //Establish an unsecure connection
            pubSubClient.connect(brokerAddress, brokerPort);
        }

        //Subscribe to all remembered topics
        this.subscribedTopicFilters.forEach(pubSubClient::subscribe);
    }

    /**
     * Creates a subscription for a given topic filter at the messaging broker.
     *
     * @param topicFilter The topic filter to subscribe to
     */
    private void addSubscription(String topicFilter) {
        //Remember subscription of this topic filter
        this.subscribedTopicFilters.add(topicFilter);

        //Let the client add the subscription
        this.pubSubClient.subscribe(topicFilter);
    }

    /**
     * Starts and manages periodic re-connect attempts when the messaging client looses its connection to the
     * messaging broker. As soon as the connection could be established again, the re-connect attempts are terminated.
     * Furthermore it is ensured that only at most one re-connect attempt is active at the same time.
     *
     * @param cause A {@link Throwable} containing the cause of the connection loss (ignored)
     */
    private void handleConnectionLoss(Throwable cause) {
        //Print information to console
        System.err.println("PubSubClient lost connection:");
        cause.printStackTrace();

        //Do nothing if reconnect attempts are already active
        if ((this.reconnectAttempt != null) && (!this.reconnectAttempt.isDone())) {
            return;
        }

        //Schedule periodic reconnect attempts
        this.reconnectAttempt = threadPool.scheduleWithFixedDelay(() -> {
            //Check if client is connected now
            if (pubSubClient.isConnected()) {
                //Connection established, thus cancel the re-connect attempts
                reconnectAttempt.cancel(true);
                return;
            }

            //Try to re-connect to the messaging broker
            connectClient();
        }, 0, RECONNECT_DELAY, TimeUnit.MILLISECONDS);
    }


    /**
     * If a secured broker is used, the initialization is delayed for 60 seconds
     * (because the authorization server is integrated and needs to startup as well).
     * The OAuth2 access token for the MBP is only valid for 10 minutes, the scheduled task ensures
     * to refresh this token every 10 minutes, if the {@link BrokerLocation} is LOCAL_SECURE or REMOTE_SECURE.
     */
    @Scheduled(initialDelay = 60000, fixedDelay = 600000)
    private void refreshOAuth2Token() {
        //Retrieve broker location from the settings service
        BrokerLocation brokerLocation = settingsService.getSettings().getBrokerLocation();

        //Check if a secure connection is desired
        if (!Arrays.asList(BrokerLocation.LOCAL_SECURE, BrokerLocation.REMOTE_SECURE).contains(brokerLocation)) {
            return;
        }

        //Request new OAuth2 access token
        requestOAuth2Token();

        //Let the client re-connect with the new token
        connectClient();
    }

    /**
     * Requests a new OAuth2 access token by using the client credentials of the MBP.
     */
    private void requestOAuth2Token() {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> request = new HttpEntity<>(new HttpHeaders());
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromHttpUrl(this.oauth2TokenUri)
                .queryParam("grant_type", this.oauth2GrantType)
                .queryParam("client-id", this.oauth2ClientId)
                .queryParam("scope", "read");
        ResponseEntity<String> response = restTemplate.exchange(uriComponentsBuilder.toUriString(), HttpMethod.POST, request, String.class);
        try {
            JSONObject body = new JSONObject(response.getBody());
            this.oauthAccessToken = body.getString("access_token");
        } catch (JSONException e) {
            //The access token request failed
            System.err.println("Could not retrieve access token: " + e.getMessage());
        }
    }

    /**
     * Transforms a given domain message to a JSON string and returns it. If the transformation failes,
     * a error message is printed and an empty JSON object is returned.
     *
     * @param message The message to transform
     * @return The resulting JSON string
     */
    private String transformMessageToJSON(DomainMessage<?> message) {
        //Transform the message
        return Json.of(message);
    }
}
