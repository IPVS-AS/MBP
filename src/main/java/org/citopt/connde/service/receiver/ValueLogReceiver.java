package org.citopt.connde.service.receiver;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.BrokerLocation;
import org.citopt.connde.service.settings.model.Settings;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Background service that receives incoming MQTT value log messages of that comply to certain topics. The service
 * implements the observer pattern which allows other other components to register themselves to the ValueLogReceiver
 * and get notified when a new value message arrives.
 */
@Service
public class ValueLogReceiver {
    //Set of MQTT topics to subscribe to
    private static final String[] SUBSCRIBE_TOPICS = {"device/#", "sensor/#", "actuator/#", "monitoring/#"};
    //URL frame of the broker to use (protocol and port, address will be filled in)
    private static final String BROKER_URL = "tcp://%s:1883";
    //Client id that is supposed to be assigned to the client instance
    private static final String CLIENT_ID = "root-server";

    //Autowired components
    private SettingsService settingsService;

    //Stores the reference of the mqtt client
    private MqttClient mqttClient = null;

    //Set ob observers that want to be notified about incoming value logs
    private Set<ValueLogReceiverObserver> observerSet;

    /**
     * Initializes the value logger service.
     *
     * @param settingsService Settings service that manages the application settings
     */
    @Autowired
    public ValueLogReceiver(SettingsService settingsService) {
        this.settingsService = settingsService;

        //Initialize set of observers
        observerSet = new HashSet<>();

        //Setup the mqtt client
        try {
            setupAndStart();
        } catch (MqttException e) {
            System.err.println("MqttException: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("IOException: " + e.getMessage());
        }
    }

    /**
     * Registers an observer at the ValueLogReceiver which then will be notified about incoming value logs.
     *
     * @param observer The observer to register
     */
    public void registerObserver(ValueLogReceiverObserver observer) {
        //Sanity check
        if (observer == null) {
            throw new IllegalArgumentException("Observer must not be null.");
        }

        //Add observer to set
        observerSet.add(observer);
    }

    /**
     * Unregisters an observer from the ValueLogReceiver which then will not be notified anymore about incoming
     * value logs.
     *
     * @param observer The observer to unregister
     */
    public void unregisterObserver(ValueLogReceiverObserver observer) {
        //Sanity check
        if (observer == null) {
            throw new IllegalArgumentException("Observer must not be null.");
        }

        //Remove observer from set
        observerSet.remove(observer);
    }

    /**
     * Unregisters all observers.
     */
    public void clearObservers() {
        observerSet.clear();
    }

    /**
     * Initializes, configures and starts the ValueLogger. The required parameters are derived from the
     * SettingsService component. If the value logger is already running, it is terminated, disconnected and
     * restarted with new settings (if they have changed in the meanwhile).
     *
     * @throws MqttException In case of an error during execution of mqtt operations
     * @throws IOException   In case of an I/O issue
     */
    public void setupAndStart() throws MqttException, IOException {
        //Disconnect the old mqtt client if already connected
        if ((mqttClient != null) && (mqttClient.isConnected())) {
            mqttClient.disconnectForcibly();
        }

        //Stores the address of the desired mqtt broker
        String brokerAddress = "localhost";

        //Determine from settings if a remote broker should be used instead
        Settings settings = settingsService.getSettings();
        if (settings.getBrokerLocation().equals(BrokerLocation.REMOTE)) {
            //Retrieve IP address of external broker from settings
            brokerAddress = settings.getBrokerIPAddress();
        }

        //Instantiate memory persistence
        MemoryPersistence persistence = new MemoryPersistence();

        //Create new mqtt client with the full broker URL
        mqttClient = new MqttClient(String.format(BROKER_URL, brokerAddress), CLIENT_ID, persistence);

        //TODO Oauth Token for MBP

        //Connect and subscribe to the topics
        mqttClient.connect();
        mqttClient.subscribe(SUBSCRIBE_TOPICS);

        //Create new callback handler for messages and register it
        MqttCallback callback = new ValueLogReceiverArrivalHandler(observerSet);
        mqttClient.setCallback(callback);
    }
}