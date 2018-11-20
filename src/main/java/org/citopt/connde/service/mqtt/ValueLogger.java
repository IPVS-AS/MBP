package org.citopt.connde.service.mqtt;

import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Background service that receives incoming value messages of actuators and sensors that comply to certain topics
 * and stores them into the value log repository for further processing.
 *
 * Created by Jan on 18.11.2018.
 */
@Service
public class ValueLogger {
    //Set of topics to subscribe to
    private static final String[] SUB_TOPICS = {"device/#", "sensor/#", "actuator/#"};

    //Autowired components
    private MqttClient mqttClient;
    private ValueLogRepository valueLogRepository;
    private ActuatorRepository actuatorRepository;
    private SensorRepository sensorRepository;

    /**
     * Creates a new value logger service.
     *
     * @param mqttClient The mqtt client to use
     * @param valueLogRepository The value log repository to write logs into
     * @param actuatorRepository The repository in which actuators are stored
     * @param sensorRepository The repository in which sensors are stored
     */
    @Autowired
    public ValueLogger(MqttClient mqttClient, ValueLogRepository valueLogRepository, ActuatorRepository actuatorRepository, SensorRepository sensorRepository) {
        //Set fields
        this.mqttClient = mqttClient;
        this.valueLogRepository = valueLogRepository;
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;

        //Setup the mqtt client
        try {
            setup();
        } catch (MqttException e) {
            System.err.println("MqttException: " + e.getMessage());
        }
    }

    /*
    Sets up the mqtt client.
     */
    private void setup() throws MqttException {
        //Connect and subscribe to the topics
        mqttClient.connect();
        mqttClient.subscribe(SUB_TOPICS);

        //Create new callback handler for messages and register it
        MqttCallback callback = new ValueLoggerEventHandler(valueLogRepository, actuatorRepository, sensorRepository);
        mqttClient.setCallback(callback);
    }
}
