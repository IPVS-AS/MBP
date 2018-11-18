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
 * Created by Jan on 18.11.2018.
 */
@Service
public class ValueLogger {

    private static final String[] SUB_TOPICS = {"device/#", "sensor/#", "actuator/#"};

    private MqttClient mqttClient;
    private ValueLogRepository valueLogRepository;
    private ActuatorRepository actuatorRepository;
    private SensorRepository sensorRepository;

    @Autowired
    public ValueLogger(MqttClient mqttClient, ValueLogRepository valueLogRepository, ActuatorRepository actuatorRepository, SensorRepository sensorRepository) {
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

    private void setup() throws MqttException {
        mqttClient.connect();
        mqttClient.subscribe(SUB_TOPICS);

        MqttCallback callback = new ValueLoggerEventHandler(valueLogRepository, actuatorRepository, sensorRepository);
        mqttClient.setCallback(callback);
    }
}
