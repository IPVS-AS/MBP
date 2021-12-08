package de.ipvs.as.mbp.service.receiver;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Set;
import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.DataModelTreeCache;
import org.bson.Document;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * then added to the value log repository.
 * Provides methods for handling incoming Mqtt events and parsing incoming value messages to value logs
 * which are then passed to the observers of the ValueLogReceiver.
 */
class ValueLogReceiverArrivalHandler implements MqttCallback {

    //JSON key names
    private static final String JSON_COMPONENT_ID = "id";
    private static final String JSON_KEY_VALUE = "value";

    // Cache of data model trees to provide fast supply
    private DataModelTreeCache dataModelTreeCache;

    //Pattern for retrieving component types from message topics
    private static final Pattern COMPONENT_TYPES_PATTERN = Pattern.compile("^([a-zA-Z0-9_-]+)/.+");

    //Set of observers
    private Set<ValueLogReceiverObserver> observerSet;

    //Repository instances for checking component IDs
    private ActuatorRepository actuatorRepository;
    private SensorRepository sensorRepository;
    private DeviceRepository deviceRepository;
    private MonitoringOperatorRepository monitoringOperatorRepository;

    /**
     * Creates a new value logger event handler.
     *
     * @param observerSet The set of observers to notify about incoming value logs.
     */
    ValueLogReceiverArrivalHandler(Set<ValueLogReceiverObserver> observerSet, ActuatorRepository actuatorRepository,
                                   SensorRepository sensorRepository, DeviceRepository deviceRepository,
                                   MonitoringOperatorRepository monitoringOperatorRepository) {
        //Store references to passed parameters
        this.observerSet = observerSet;
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.monitoringOperatorRepository = monitoringOperatorRepository;

        // Get the bean of the data model tree cache
        this.dataModelTreeCache = DynamicBeanProvider.get(DataModelTreeCache.class);
    }

    /**
     * Handles the case that the mqtt client lost connection to the broker.
     *
     * @param throwable Throwable that indicates the issue
     */
    @Override
    public void connectionLost(Throwable throwable) {
        System.err.println("Mqtt client lost connection.");
        try {
            throw new MqttException(throwable);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles incoming m
     * qtt messages, i.e. parses the incoming value message to a value log which is then
     * passed to the observers of the ValueLogReceiver.
     *
     * @param topic       The topic under which the message was sent
     * @param mqttMessage The received value log message
     * @throws JSONException  In case the message could not be parsed
     * @throws ParseException In case a date value field could not be parsed
     */
    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws JSONException, ParseException {
        //Catch errors during message processing to avoid crashes of the receiver
        try {
            //Record current time
            Instant time = ZonedDateTime.now().toInstant();

            //Extract QoS
            int qos = mqttMessage.getQos();

            //Extract message string from the message object
            String message = new String(mqttMessage.getPayload());

            //Create a json object from the message
            JSONObject json = new JSONObject(message);

            //Create new empty value log
            ValueLog valueLog = new ValueLog();

            //Retrieve component ID from message
            String componentID = json.getString(JSON_COMPONENT_ID);

            //Extract component type from topic under which the message was published
            String componentType = extractComponentType(topic);

            //Check component ID for validity
            if (!isComponentIDValid(componentID, componentType)) {
                System.out.println("Value with invalid component ID \"" + componentID + "\" received");
                return;
            }

            //Set value log fields
            valueLog.setTopic(topic);
            valueLog.setMessage(message);
            valueLog.setQos(qos);
            valueLog.setTime(time);
            valueLog.setIdref(componentID);
            if (!componentType.toLowerCase().equals("monitoring")) {
                // Validate the value object part of the json object and transfer the json to a document representation
                valueLog.setValue(ValueLogReceiveVerifier.validateJsonValueAndGetDocument(
                        // Retrieve the root node of the value json object which must be part of the mqtt message by convention
                        json.getJSONObject(JSON_KEY_VALUE),
                        // Get the data model tree of the component as this is needed to infer the right database types
                        dataModelTreeCache.getDataModelOfComponent(componentID)
                ));
            } else {
                // Extra case for monitoring operators as they don't have data models and are just expected to send numbers
                double monitoringOperatorVal = json.getDouble(JSON_KEY_VALUE);
                Document valueDoubleDoc = new Document();
                valueDoubleDoc.append(JSON_KEY_VALUE, monitoringOperatorVal);
                valueLog.setValue(valueDoubleDoc);
            }
            valueLog.setComponent(componentType);

            //Notify all observers
            notifyObservers(valueLog);
        } catch (Exception e) {
            System.err.println("Value log processing failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Handle events that are triggered when the delivery of a message was completed.
     *
     * @param iMqttDeliveryToken Delivery token of the message
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    /**
     * Notifies all observers of the ValueLogReceiver about the received value log.
     *
     * @param valueLog The received value log
     */
    public void notifyObservers(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        //Iterate over all observers and notify them
        for (ValueLogReceiverObserver observer : observerSet) {
            observer.onValueReceived(valueLog);
        }
    }

    /**
     * Checks and returns whether a given component ID is valid for a given component type.
     *
     * @param componentID   The component ID to check
     * @param componentType The corresponding component type to check
     * @return True, if the component ID is valid for this type; false otherwise
     */
    private boolean isComponentIDValid(String componentID, String componentType) {
        //Sanity checks
        if ((componentID == null) || componentID.isEmpty() || (componentType == null) || (componentType.isEmpty())) {
            return false;
        }

        //Check component type
        if (componentType.equalsIgnoreCase(new Actuator().getComponentTypeName())) {
            //Component is actuator, check if component exists
            return actuatorRepository.existsById(componentID);
        } else if (componentType.equalsIgnoreCase(new Sensor().getComponentTypeName())) {
            //Component is sensor, check if component exists
            return sensorRepository.existsById(componentID);
        } else if (componentType.equalsIgnoreCase(new MonitoringComponent().getComponentTypeName())) {
            //Component is monitoring component, create monitoring component object
            MonitoringComponent monitoringComponent = new MonitoringComponent(componentID);

            //Retrieve monitoring operator and device
            Optional<MonitoringOperator> monitoringOperator = monitoringOperatorRepository.findById(monitoringComponent.getMonitoringOperatorID());
            Optional<Device> device = deviceRepository.findById(monitoringComponent.getDeviceID());

            //Check if monitoring operator and device exist
            if (!monitoringOperator.isPresent()) {
                return false;
            } else if (!device.isPresent()) {
                return false;
            }

            //Check compatibility between monitoring operator and device
            return monitoringOperator.get().isCompatibleWith(device.get().getComponentType());
        }

        return false;
    }

    /**
     * Extracts the type of a component from the topic under which the component published a message.
     *
     * @param topic The topic to evaluate
     * @return The determined component type or null, if invalid
     */
    private String extractComponentType(String topic) {
        //Sanity checks
        if ((topic == null) || topic.isEmpty()) return null;

        //Try to extract the component type by using the regular expression pattern
        Matcher matcher = COMPONENT_TYPES_PATTERN.matcher(topic);

        //Check if the matches of interest could be found
        if (!matcher.find()) return null;

        //Return group result containing the component type
        return matcher.group(1);
    }
}