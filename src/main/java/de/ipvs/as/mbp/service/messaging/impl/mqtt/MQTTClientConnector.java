package de.ipvs.as.mbp.service.messaging.impl.mqtt;

import de.ipvs.as.mbp.service.messaging.PubSubClient;
import de.ipvs.as.mbp.service.messaging.handler.PubSubConnectionLossHandler;
import de.ipvs.as.mbp.service.messaging.handler.PubSubExceptionHandler;
import de.ipvs.as.mbp.service.messaging.handler.PubSubMessageHandler;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Connector to an actual MQTT client that enables publish-subscribe-based messaging via MQTT
 * in cooperation with an external messaging broker. The connector implements the {@link PubSubClient} interface and
 * thus exposes its functionality via uniform methods.
 */
public class MQTTClientConnector implements PubSubClient {

    //Template of the broker's host URL
    private static final String BROKER_URL_TEMPLATE = "tcp://%s:%d";

    //Timeout in milliseconds for finishing work before disconnecting (may be zero)
    private static final int DISCONNECT_TIMEOUT = 1000;

    //ID that is assigned to the client with an unique suffix in order to avoid name collisions
    private static final String CLIENT_ID = "mbp-client-" + generateUniqueID();

    //The actual MQTT client to use
    private MqttClient mqttClient = null;

    //Memory persistence to use for the client
    private MemoryPersistence memoryPersistence;

    //Callback object to use for handling asynchronous events
    private MqttCallback mqttCallback;

    //Handlers for messages, exceptions and connection losses
    private PubSubMessageHandler messageHandler;
    private PubSubExceptionHandler exceptionHandler;
    private PubSubConnectionLossHandler connectionLossHandler;

    /**
     * Initializes the MQTT client connector.
     */
    public MQTTClientConnector() {
        //Create memory persistence and callback object
        this.memoryPersistence = new MemoryPersistence();
        this.mqttCallback = new MQTTClientCallback(this);
    }


    /**
     * Establishes an unsecured connection to the publish-subscribe messaging broker that is available at a given
     * host address with a given port. In case there is already an active connection to the messaging broker,
     * this connection is gracefully aborted and a new connection is established.
     *
     * @param hostAddress The host address of the messaging broker
     * @param port        The port of the messaging broker
     */
    @Override
    public void connect(String hostAddress, int port) {
        try {
            //Create or re-create the MQTT client
            createMQTTClient(hostAddress, port);

            //Let the client connect to the broker
            this.mqttClient.connect();
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }
    }

    /**
     * Establishes a secured connection to the publish-subscribe messaging broker that is available at a given
     * host address with a given port, by using a given username and password for authentication. In case there is
     * already an active connection to the messaging broker, this connection is gracefully aborted and a new connection
     * is established.
     *
     * @param hostAddress The host address of the messaging broker
     * @param port        The port of the messaging broker
     * @param username    The username to use for authentication
     * @param password    The password to use for authentication
     */
    @Override
    public void connectSecure(String hostAddress, int port, String username, String password) {
        try {
            //Create or re-create the MQTT client
            createMQTTClient(hostAddress, port);

            //Create connect options to enable a secure connection
            MqttConnectOptions connectOptions = createConnectOptions(username, password);

            //Let the client connect to the broker using the options
            this.mqttClient.connect(connectOptions);
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }
    }

    /**
     * Gracefully disconnects from the publish-subscribe messaging broker in case a connection was
     * previously established.
     */
    @Override
    public void disconnect() {
        //Check if client is initialized and connected
        if (!isConnected()) {
            return;
        }

        try {
            //Disconnect the client with timeout
            this.mqttClient.disconnect(DISCONNECT_TIMEOUT);
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }
    }

    /**
     * Disconnects and destroys the client such that all allocated resources are released.
     */
    @Override
    public void close() {
        //Check if client is initialized
        if (this.mqttClient == null) {
            return;
        }

        try {
            //Check if client is connected
            if (this.mqttClient.isConnected()) {
                //Disconnect client with timeout
                this.mqttClient.disconnect(DISCONNECT_TIMEOUT);
            }

            //Release resources
            this.mqttClient.close();
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }

        //Unset the client reference
        this.mqttClient = null;
    }

    /**
     * Returns whether there is currently an active connection to the publish-subscribe messaging broker.
     *
     * @return True, if a connection exists; false otherwise
     */
    @Override
    public boolean isConnected() {
        //Check if MQTT client is initialized and connected
        return (this.mqttClient != null) && (this.mqttClient.isConnected());
    }

    /**
     * Publishes a given string message under a given topic at the publish-subscribe messaging broker.
     *
     * @param topic   The topic under which the message is supposed to be published
     * @param message The message to publish
     */
    @Override
    public void publish(String topic, String message) {
        //Check if client is initialized and connected
        requireInitialized();

        //Sanity check for parameters
        if ((topic == null) || (topic.isEmpty())) {
            throw new IllegalArgumentException("The topic must not be empty.");
        } else if ((message == null) || (message.isEmpty())) {
            throw new IllegalArgumentException("The message must not be empty.");
        }

        //Create message
        MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));

        //Publish message
        try {
            this.mqttClient.publish(topic, mqttMessage);
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }
    }

    /**
     * Registers a subscription for a given topic filter at the publish-subscribe messaging broker, such that
     * the client will be notified about future messages that are published at the broker under a topic that matches
     * the filter.
     *
     * @param topicFilter The topic filter to use for the subscription
     */
    @Override
    public void subscribe(String topicFilter) {
        //Check if client is initialized and connected
        requireInitialized();

        //Sanity check for topic filter
        if ((topicFilter == null) || (topicFilter.isEmpty())) {
            throw new IllegalArgumentException("The topic filter must not be empty.");
        }

        try {
            //Perform subscription
            this.mqttClient.subscribe(topicFilter);
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }
    }

    /**
     * Unregisters a subscription for a given topic filter at the publish-subscribe messaging broker. This can only be
     * done if exactly the same topic filter is passed that was also used for the creation of the subscription.
     *
     * @param topicFilter The topic filter to unsubscribe
     */
    @Override
    public void unsubscribe(String topicFilter) {
        //Check if client is initialized and connected
        requireInitialized();

        //Sanity check for topic filter
        if ((topicFilter == null) || (topicFilter.isEmpty())) {
            throw new IllegalArgumentException("The topic filter must not be empty.");
        }

        try {
            //Perform subscription
            this.mqttClient.unsubscribe(topicFilter);
        } catch (MqttException e) {
            //Handle the exception
            handleException(e);
        }
    }

    /**
     * Registers a message handler that is notified about all messages that are published at the publish-subscribe
     * messaging broker under a topic that matches at least one of the topic filters for which subscriptions were
     * previously created.
     *
     * @param messageHandler The message handler to set
     */
    @Override
    public void setMessageHandler(PubSubMessageHandler messageHandler) {
        //Set message handler
        this.messageHandler = messageHandler;
    }

    /**
     * Registers an exception handler that is notified about all client-side and messaging-related exceptions
     * that occur while working with the publish-subscribe messaging broker.
     *
     * @param exceptionHandler The exception handler to set
     */
    @Override
    public void setExceptionHandler(PubSubExceptionHandler exceptionHandler) {
        //Set exception handler
        this.exceptionHandler = exceptionHandler;
    }

    /**
     * Registers an connection loss handler that is notified when the connection to the publish-subscribe messaging
     * broker is lost.
     *
     * @param connectionLossHandler The connection loss handler to set
     */
    @Override
    public void setConnectionLossHandler(PubSubConnectionLossHandler connectionLossHandler) {
        //Set connection loss handler
        this.connectionLossHandler = connectionLossHandler;
    }

    /**
     * Returns whether a given topic matches a given topic filter, according to the topic pattern that is used by
     * the publish-subscribe message broker. Since this check typically only includes syntactic comparisons,
     * no active connection to the broker is required for the execution of this method.
     *
     * @param topic       The topic to check
     * @param topicFilter The topic filter to check the topic against
     * @return True, if the topic matches the topic filter; false otherwise
     */
    @Override
    public boolean topicMatchesFilter(String topic, String topicFilter) {
        return MqttTopic.isMatched(topicFilter, topic);
    }

    /**
     * Returns the currently active message handler of the connector or null if none was set.
     *
     * @return THe current message handler
     */
    protected PubSubMessageHandler getMessageHandler() {
        return this.messageHandler;
    }

    /**
     * Returns the currently active exception handler of the connector or null if none was set.
     *
     * @return The current exception handler
     */
    protected PubSubExceptionHandler getExceptionHandler() {
        return this.exceptionHandler;
    }

    /**
     * Returns the currently active connection less handler of the connector or null if none was set.
     *
     * @return The current connection loss handler
     */
    protected PubSubConnectionLossHandler getConnectionLossHandler() {
        return this.connectionLossHandler;
    }

    /**
     * Checks whether the MQTT client is initialized, but not necessarily connected to the MQTT messaging broker.
     * An exception is thrown if not initialization was done.
     */
    private void requireInitialized() {
        //Check if client is initialized
        if (this.mqttClient == null) {
            throw new IllegalStateException("The MQTT client is not initialized.");
        }
    }

    /**
     * Creates a new MQTT client that is able to connect to a MQTT messaging broker at a given host address
     * with a given port.
     *
     * @param hostAddress The host address of the messaging broker
     * @param port        The port of the messaging broker
     * @throws MqttException In case of an unexpected MQTT-related failure
     */
    private void createMQTTClient(String hostAddress, int port) throws MqttException {
        //Validate parameters and put broker URL together
        String brokerURL = getBrokerHostAddress(hostAddress, port);

        //Check if client is already initialized
        if (this.mqttClient != null) {
            //Check if the client is connected
            if (this.mqttClient.isConnected()) {
                //Disconnect the client gracefully
                this.mqttClient.disconnect(DISCONNECT_TIMEOUT);
            }

            //Destroy client and release resources
            this.mqttClient.close();
        }

        //Create new MQTT client
        this.mqttClient = new MqttClient(brokerURL, CLIENT_ID, memoryPersistence);

        //Configure the new MQTT client
        this.mqttClient.setManualAcks(false);
        this.mqttClient.setCallback(this.mqttCallback);
    }

    /**
     * Creates and returns MQTT connect options from a given username and password that can be used to authenticate
     * the client at the external MQTT messaging broker. The options object as resulting from this method is required
     * for establishing secure connections.
     *
     * @param username The username to use for authentication
     * @param password The password to use for authentication
     * @return The resulting MQTT connect options
     */
    private MqttConnectOptions createConnectOptions(String username, String password) {
        //Create empty connect options object
        MqttConnectOptions connectOptions = new MqttConnectOptions();

        //Adjust the options to enable authentication with username and password
        connectOptions.setCleanSession(true);
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());

        //Return the resulting connect options
        return connectOptions;
    }

    /**
     * Handles a given MQTT exception by either passing it to a exception handler (if set) or by printing the exception
     * to the standard output.
     *
     * @param exception The exception to handle
     */
    private void handleException(MqttException exception) {
        //Check if exception handler is set
        if (exceptionHandler != null) {
            exceptionHandler.handleException(exception);
            return;
        }

        //No exception handler set, print to standard output
        System.err.printf("%s: %s%n", exception.getClass().getSimpleName(), exception.getMessage());
        exception.printStackTrace();
    }

    /**
     * Takes the host address and port of an MQTT messaging broker and puts them together in order to form and return
     * the full host URL of the broker. In addition, sanity checks are applied and exception thrown in case the
     * provided parameters are invalid.
     *
     * @param hostAddress The host address of the MQTT broker
     * @param port        The port of the MQTT broker
     * @return The resulting full host address of the MQTT broker
     */
    private static String getBrokerHostAddress(String hostAddress, int port) {
        //Sanity check
        if ((hostAddress == null) || hostAddress.isEmpty()) {
            throw new IllegalArgumentException("No MQTT broker host address provided.");
        } else if ((port < 10) || (port > 65535)) {
            throw new IllegalArgumentException("Invalid MQTT broker port number provided.");
        }

        //Put broker host URL together
        String brokerURL = String.format(BROKER_URL_TEMPLATE, hostAddress, port).toLowerCase();

        //Validate resulting host address
        if (!(new UrlValidator(new String[]{"http", "https", "udp", "tcp"}, UrlValidator.ALLOW_LOCAL_URLS).isValid(brokerURL))) {
            throw new IllegalArgumentException("Resulting MQTT broker URL is invalid.");
        }

        //Return result
        return brokerURL;
    }

    /**
     * Creates an unique identifier that may be appended to a MQTT client ID in order to avoid name collisions.
     *
     * @return The generated unique identifier
     */
    private static String generateUniqueID() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
