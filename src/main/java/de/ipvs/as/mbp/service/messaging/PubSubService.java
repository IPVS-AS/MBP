package de.ipvs.as.mbp.service.messaging;

import de.ipvs.as.mbp.domain.settings.BrokerLocation;
import de.ipvs.as.mbp.domain.settings.Settings;
import de.ipvs.as.mbp.service.messaging.dispatcher.MessageDispatcher;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.JSONMessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.MessageListener;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.SubscriptionMessageListener;
import de.ipvs.as.mbp.service.settings.SettingsService;
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

/**
 * This service offers technology-agnostic messaging functions for interacting with publish-subscribe-based middleware
 * that is connected to the MBP.
 */
@Service
@EnableScheduling
@PropertySource(value = "classpath:application.properties")
public class PubSubService {
    //Port at which the messaging broker listens
    private static final int BROKER_PORT = 1883;

    @Autowired
    private PubSubClient pubSubClient;

    @Autowired
    private SettingsService settingsService;

    //Dispatcher for incoming messages
    private MessageDispatcher messageDispatcher;

    //Remembers all topics that are currently subscribed
    private Set<String> subscribedTopicFilters;

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
     * Creates and initializes the service for a given client that enables publish-subscribe-based messaging.
     */
    @Autowired
    public PubSubService() {
        //Initialize sub-components and data structures
        this.messageDispatcher = new MessageDispatcher(pubSubClient);
        this.subscribedTopicFilters = new HashSet<>();

        //Set a message handler that delegates incoming messages to the dispatcher
        pubSubClient.setMessageHandler(messageDispatcher::dispatchMessage);

        //Let the client connect
        connectClient();
    }


    public void publish(String topic, JSONObject jsonObject) {
        //Transform provided JSON message to string and publish it
        publish(topic, jsonObject.toString());
    }

    public void publish(String topic, String message) {
        //Publish message via the client
        pubSubClient.publish(topic, message);
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
        //Add subscription
        addSubscription(topicFilter);

        //Update dispatcher
        this.messageDispatcher.subscribe(topicFilter, listener);
    }

    public void subscribeJSON(String topicFilter, JSONMessageListener listener) {
        //Add subscription
        addSubscription(topicFilter);

        //Update dispatcher
        this.messageDispatcher.subscribeJSON(topicFilter, listener);
    }

    public void subscribe(List<String> topicFilter, MessageListener listener) {
        //Create one subscription for each topic filter
        topicFilter.forEach(t -> subscribe(t, listener));
    }

    public void subscribeJSON(List<String> topicFilter, JSONMessageListener listener) {
        //Create one subscription for each topic filter
        topicFilter.forEach(t -> subscribeJSON(t, listener));
    }

    public void unsubscribe(String topicFilter, SubscriptionMessageListener listener) {
        //Remove subscription from dispatcher
        boolean remainingSubscriptions = this.messageDispatcher.unsubscribe(topicFilter, listener);

        //Check if no subscriptions remain for this topic filter
        if (!remainingSubscriptions) {
            //Remove topic filter from set of subscribed topic filters
            this.subscribedTopicFilters.remove(topicFilter);
        }
    }

    private void connectClient() {
        //Retrieve broker location and URL from the settings service
        Settings settings = settingsService.getSettings();
        BrokerLocation brokerLocation = settings.getBrokerLocation();
        String brokerAddress = settings.getBrokerIPAddress();

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
            pubSubClient.connectSecure(brokerAddress, BROKER_PORT, this.oauthAccessToken, "any");
        } else {
            //Establish an unsecure connection
            pubSubClient.connect(brokerAddress, BROKER_PORT);
        }

        //Subscribe to all remembered topics
        this.subscribedTopicFilters.forEach(t -> pubSubClient.subscribe(t));
    }

    private void addSubscription(String topicFilter) {
        //Remember subscription of this topic filter
        this.subscribedTopicFilters.add(topicFilter);

        //Let the client add the subscription
        this.pubSubClient.subscribe(topicFilter);
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
        } catch (JSONException ignored) {
        }
    }
}
