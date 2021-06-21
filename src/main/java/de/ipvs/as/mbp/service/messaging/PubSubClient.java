package de.ipvs.as.mbp.service.messaging;

import de.ipvs.as.mbp.service.messaging.handler.PubSubConnectionLossHandler;
import de.ipvs.as.mbp.service.messaging.handler.PubSubExceptionHandler;
import de.ipvs.as.mbp.service.messaging.handler.PubSubMessageHandler;

/**
 * Collection of technology-agnostic interfaces that a publish-subscribe middleware client must implement
 * in order to be usable within the MBP. The purpose of this interface is to abstract from concrete
 * messaging technologies and their specific functionality and to offer uniform publish-subscribe interfaces
 * to the various MBP components.
 */
public interface PubSubClient {
    /**
     * Establishes an unsecured connection to the publish-subscribe messaging broker that is available at a given
     * host address with a given port. In case there is already an active connection to the messaging broker,
     * this connection is gracefully aborted and a new connection is established.
     *
     * @param hostAddress The host address of the messaging broker
     * @param port    The port of the messaging broker
     */
    void connect(String hostAddress, int port);

    /**
     * Establishes a secured connection to the publish-subscribe messaging broker that is available at a given
     * host address with a given port, by using a given username and password for authentication. In case there is
     * already an active connection to the messaging broker, this connection is gracefully aborted and a new connection
     * is established.
     *
     * @param hostAddress  The host address of the messaging broker
     * @param port     The port of the messaging broker
     * @param username The username to use for authentication
     * @param password The password to use for authentication
     */
    void connectSecure(String hostAddress, int port, String username, String password);

    /**
     * Gracefully disconnects from the publish-subscribe messaging broker in case a connection was
     * previously established.
     */
    void disconnect();

    /**
     * Disconnects and destroys the client such that all allocated resources are released.
     */
    void close();

    /**
     * Returns whether there is currently an active connection to the publish-subscribe messaging broker.
     *
     * @return True, if a connection exists; false otherwise
     */
    boolean isConnected();

    /**
     * Publishes a given string message under a given topic at the publish-subscribe messaging broker.
     *
     * @param topic   The topic under which the message is supposed to be published
     * @param message The message to publish
     */
    void publish(String topic, String message);

    /**
     * Registers a subscription for a given topic filter at the publish-subscribe messaging broker, such that
     * the client will be notified about future messages that are published at the broker under a topic that matches
     * the filter.
     *
     * @param topicFilter The topic filter to use for the subscription
     */
    void subscribe(String topicFilter);

    /**
     * Unregisters a subscription for a given topic filter at the publish-subscribe messaging broker. This can only be
     * done if exactly the same topic filter is passed that was also used for the creation of the subscription.
     *
     * @param topicFilter The topic filter to unsubscribe
     */
    void unsubscribe(String topicFilter);

    /**
     * Registers a message handler that is notified about all messages that are published at the publish-subscribe
     * messaging broker under a topic that matches at least one of the topic filters for which subscriptions were
     * previously created.
     *
     * @param messageHandler The message handler to set
     */
    void setMessageHandler(PubSubMessageHandler messageHandler);

    /**
     * Registers an exception handler that is notified about all client-side and messaging-related exceptions
     * that occur while working with the publish-subscribe messaging broker.
     *
     * @param exceptionHandler The exception handler to set
     */
    void setExceptionHandler(PubSubExceptionHandler exceptionHandler);

    /**
     * Registers an connection loss handler that is notified when the connection to the publish-subscribe messaging
     * broker is lost.
     *
     * @param connectionLossHandler The connection loss handler to set
     */
    void setConnectionLossHandler(PubSubConnectionLossHandler connectionLossHandler);

    /**
     * Returns whether a given topic matches a given topic filter, according to the topic pattern that is used by
     * the publish-subscribe message broker. Since this check typically only includes syntactic comparisons,
     * no active connection to the broker is required for the execution of this method.
     *
     * @param topic       The topic to check
     * @param topicFilter The topic filter to check the topic against
     * @return True, if the topic matches the topic filter; false otherwise
     */
    boolean topicMatchesFilter(String topic, String topicFilter);
}
