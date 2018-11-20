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
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Provides methods for handling incoming Mqtt events and parses incoming actuator/sensor value messages which are
 * then added to the value log repository.
 *
 * Created by Jan on 18.11.2018.
 */
public class ValueLoggerEventHandler implements MqttCallback {

    //Format in which dates are stores
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //Identifier strings for actuators and sensors
    private static final String IDENTIFIER_ACTUATOR = "ACTUATOR";
    private static final String IDENTIFIER_SENSOR = "SENSOR";

    //JSON key names
    private static final String JSON_KEY_COMPONENT_TYPE = "component";
    private static final String JSON_COMPONENT_ID = "id";
    private static final String JSON_KEY_VALUE = "value";


    //Repository beans
    private ValueLogRepository valueLogRepository;
    private ActuatorRepository actuatorRepository;
    private SensorRepository sensorRepository;

    /**
     * Creates a new value logger event handler.
     *
     * @param valueLogRepository The value log repository to write logs into
     * @param actuatorRepository The repository in which actuators are stored
     * @param sensorRepository The repository in which sensors are stored
     */
    protected ValueLoggerEventHandler(ValueLogRepository valueLogRepository, ActuatorRepository actuatorRepository, SensorRepository sensorRepository) {
        this.valueLogRepository = valueLogRepository;
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;
    }

    /**
     * Handles the case that the mqtt client lost connection to the broker.
     * @param throwable Throwable that indicates the issue
     */
    @Override
    public void connectionLost(Throwable throwable) {
        System.err.println("Mqtt client lost connection.");
    }

    /**
     * Handles incoming mqtt messages, i.e. parses actuator/sensor message, creates a value log entry and adds it
     * to the repository.
     *
     * @param topic The topic under which the message was sent
     * @param mqttMessage The received message
     * @throws JSONException In case the message could not be parsed
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws JSONException {
        //Get QoS and date in the appropriate format
        int qos = mqttMessage.getQos();
        String date = DATE_FORMAT.format(new Date());

        //Extract message string from the message object
        String message = new String(mqttMessage.getPayload());

        //Create a json object from the message
        JSONObject json = new JSONObject(message);

        //Extract all required data from the message and add it to a new value log object
        ValueLog valueLog = new ValueLog();

        String componentType = json.getString(JSON_KEY_COMPONENT_TYPE);
        String componentID = json.getString(JSON_COMPONENT_ID);

        valueLog.setTopic(topic);
        valueLog.setMessage(message);
        valueLog.setQos(qos);
        valueLog.setDate(date);
        valueLog.setIdref(componentID);
        valueLog.setValue(json.getString(JSON_KEY_VALUE));
        valueLog.setComponent(componentType);

        //Lookup the object of the actuator/sensor the message origins from and reference it
        if (componentType.equals(IDENTIFIER_ACTUATOR)) {
            Actuator actuator = actuatorRepository.findOne(componentID);
            valueLog.setActuatorRef(actuator);
        } else if (componentType.equals(IDENTIFIER_SENSOR)) {
            Sensor sensor = sensorRepository.findOne(componentID);
            valueLog.setSensorRef(sensor);
        }

        //Insert log into repository
        valueLogRepository.insert(valueLog);
    }

    /**
     * Handle events that are triggered when the delivery of a message was completed.
     * @param iMqttDeliveryToken Delivery token of the message
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }
}
