package org.citopt.connde.service.mqtt;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Jan on 18.11.2018.
 */
public class ValueLoggerEventHandler implements MqttCallback {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private ValueLogRepository valueLogRepository;
    private ActuatorRepository actuatorRepository;
    private SensorRepository sensorRepository;

    public ValueLoggerEventHandler(ValueLogRepository valueLogRepository, ActuatorRepository actuatorRepository, SensorRepository sensorRepository) {
        this.valueLogRepository = valueLogRepository;
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;
    }

    @Override
    public void connectionLost(Throwable throwable) {
        System.err.println("Mqtt client lost connection.");
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

        String message = new String(mqttMessage.getPayload());
        int qos = mqttMessage.getQos();
        String date = DATE_FORMAT.format(new Date());

        JSONObject json = new JSONObject(message);

        String componentType = json.getString("component");
        String componentID = json.getString("id");

        ValueLog valueLog = new ValueLog();
        valueLog.setTopic(topic);
        valueLog.setMessage(message);
        valueLog.setQos(qos);
        valueLog.setDate(date);

        valueLog.setIdref(componentID);
        valueLog.setValue(json.getString("value"));
        valueLog.setComponent(componentType);


        if (componentType.equals("ACTUATOR")) {
            Actuator actuator = actuatorRepository.findOne(componentID);
            valueLog.setActuatorRef(actuator);
        } else if (componentType.equals("SENSOR")) {
            Sensor sensor = sensorRepository.findOne(componentID);
            valueLog.setSensorRef(sensor);
        }

        //Insert log into repository
        valueLogRepository.insert(valueLog);

        System.out.println("New message with topic \"" + topic + "\": " + message);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
