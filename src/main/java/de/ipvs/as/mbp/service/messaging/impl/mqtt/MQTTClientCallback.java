package de.ipvs.as.mbp.service.messaging.impl.mqtt;

import de.ipvs.as.mbp.service.messaging.handler.PubSubConnectionLossHandler;
import de.ipvs.as.mbp.service.messaging.handler.PubSubMessageHandler;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Handles callbacks for asynchronous MQTT events, e.g. when a message arrived or when the connection is lost.
 */
public class MQTTClientCallback implements MqttCallback {
    //Delay before trying to reconnect in case of connection losses
    private static final int RECONNECT_DELAY = 10 * 1000;

    //Thread pool for asynchronous reconnecting in case of connection losses
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

    //The MQTT client that makes use of this callback
    private MQTTClientConnector mqttClient;

    public MQTTClientCallback(MQTTClientConnector mqttClient) {
        this.mqttClient = mqttClient;
    }

    /**
     * This method is called when the connection to the server is lost.
     *
     * @param cause the reason behind the loss of connection.
     */
    @Override
    public void connectionLost(Throwable cause) {
        //Retrieve connection loss handler from the MQTT client
        PubSubConnectionLossHandler connectionLossHandler = mqttClient.getConnectionLossHandler();

        //Check if a connection loss handler is set
        if (connectionLossHandler == null) {
            return;
        }

        //Notify the connection loss handler
        connectionLossHandler.handleConnectionLoss(cause);
    }

    /**
     * This method is called when a message arrives from the server.
     *
     * <p>
     * This method is invoked synchronously by the MQTT client. An
     * acknowledgment is not sent back to the server until this
     * method returns cleanly.</p>
     * <p>
     * If an implementation of this method throws an <code>Exception</code>, then the
     * client will be shut down.  When the client is next re-connected, any QoS
     * 1 or 2 messages will be redelivered by the server.</p>
     * <p>
     * Any additional messages which arrive while an
     * implementation of this method is running, will build up in memory, and
     * will then back up on the network.</p>
     * <p>
     * If an application needs to persist data, then it
     * should ensure the data is persisted prior to returning from this method, as
     * after returning from this method, the message is considered to have been
     * delivered, and will not be reproducible.</p>
     * <p>
     * It is possible to send a new message within an implementation of this callback
     * (for example, a response to this message), but the implementation must not
     * disconnect the client, as it will be impossible to send an acknowledgment for
     * the message being processed, and a deadlock will occur.</p>
     *
     * @param topic   name of the topic on the message was published to
     * @param message the actual message.
     * @throws Exception if a terminal error has occurred, and the client should be
     *                   shut down.
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //Sanity check
        if (message == null) {
            return;
        }

        //Retrieve message handler from the MQTT client
        PubSubMessageHandler messageHandler = mqttClient.getMessageHandler();

        //Check if a message handler is set
        if (messageHandler == null) {
            return;
        }

        //Extract payload from message and transform it to a string
        String messagePayload = new String(message.getPayload());

        //Let the message handler handle the message
        messageHandler.handleMessage(topic, messagePayload);
    }

    /**
     * Called when delivery for a message has been completed, and all
     * acknowledgments have been received. For QoS 0 messages it is
     * called once the message has been handed to the network for
     * delivery. For QoS 1 it is called when PUBACK is received and
     * for QoS 2 when PUBCOMP is received. The token will be the same
     * token as that returned when the message was published.
     *
     * @param token the delivery token associated with the message.
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        //Do nothing yet
    }
}
