package org.citopt.connde.service.mqtt;

import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.BrokerLocation;
import org.citopt.connde.service.settings.model.Settings;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Background service that receives incoming value messages of actuators and sensors that comply to certain topics
 * and stores them into the value log repository for further processing.
 *
 * @author Jan
 */
@Service
public class ValueLogger {
    //Set of topics to subscribe to
    private static final String[] SUBSCRIBE_TOPICS = {"device/#", "sensor/#", "actuator/#", "monitoring/#"};
    //URL frame of the broker to use (protocol and port, address will be filled in)
    private static final String BROKER_URL = "tcp://%s:1883";
    //Client id that is supposed to be assigned to the client instance
    private static final String CLIENT_ID = "root-server";

    //Autowired components
    private SettingsService settingsService;
    private ValueLogRepository valueLogRepository;

    //Stores the reference of the mqtt client
    private MqttClient mqttClient = null;

    /**
     * Initializes the value logger service.
     *
     * @param settingsService    Settings service that manages the application settings
     * @param valueLogRepository The value log repository to write logs into
     */
    @Autowired
    public ValueLogger(SettingsService settingsService, ValueLogRepository valueLogRepository) {
        this.settingsService = settingsService;
        this.valueLogRepository = valueLogRepository;

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

        //Connect and subscribe to the topics
        mqttClient.connect();
        mqttClient.subscribe(SUBSCRIBE_TOPICS);

        //Create new callback handler for messages and register it
        MqttCallback callback = new ValueLoggerEventHandler(valueLogRepository);
        mqttClient.setCallback(callback);
    }
}
