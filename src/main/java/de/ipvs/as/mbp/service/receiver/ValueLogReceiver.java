
package de.ipvs.as.mbp.service.receiver;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * Background service that receives incoming value log messages from the publish-subscribe-based messaging service.
 * In order to be treated and processed as value logs, the topics of the incoming messages must comply to certain
 * topics filters. This service implements the observer pattern which allows other components to register themselves
 * to the ValueLogReceiver and get notified in case a new value log from a device arrives at the MBP.
 */
@Service
@EnableScheduling
public class ValueLogReceiver {
    //List of topic filters to subscribe to
    private static final List<String> SUBSCRIBE_TOPIC_FILTERS = Arrays.asList("device/#", "sensor/#", "actuator/#", "monitoring/#");

    //JSON key names of value log messages
    private static final String JSON_KEY_COMPONENT_TYPE = "component";
    private static final String JSON_COMPONENT_ID = "id";
    private static final String JSON_KEY_VALUE = "value";

    //Set ob observers which want to be notified about incoming value logs
    private final Set<ValueLogObserver> observerSet;

    //References to repositories to check the validity of incoming value logs
    private final ActuatorRepository actuatorRepository;
    private final SensorRepository sensorRepository;
    private final DeviceRepository deviceRepository;
    private final MonitoringOperatorRepository monitoringOperatorRepository;

    /**
     * Initializes the value log receiver service.
     *
     * @param pubSubService                The messaging service for receiving the value logs (auto-wired)
     * @param actuatorRepository           Repository in which the actuators are stored (auto-wired)
     * @param sensorRepository             Repository in which the sensors are stored (auto-wired)
     * @param deviceRepository             Repository in which the devices are stored (auto-wired)
     * @param monitoringOperatorRepository Repository in which the monitoring operators are stored (auto-wired)
     */
    @Autowired
    public ValueLogReceiver(PubSubService pubSubService, ActuatorRepository actuatorRepository,
                            SensorRepository sensorRepository, DeviceRepository deviceRepository,
                            MonitoringOperatorRepository monitoringOperatorRepository) {
        //Store component references
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;
        this.deviceRepository = deviceRepository;
        this.monitoringOperatorRepository = monitoringOperatorRepository;

        //Initialize the set of observers
        observerSet = new HashSet<>();

        //Subscribe to all topics that are relevant for receiving the value logs
        pubSubService.subscribeJSON(SUBSCRIBE_TOPIC_FILTERS, (m, t, tf) -> processValueLogMessage(m, t));
    }


    /**
     * Registers an observer at the ValueLogReceiver which then will be notified about incoming value logs.
     *
     * @param observer The observer to register
     */
    public void registerObserver(ValueLogObserver observer) {
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
    public void unregisterObserver(ValueLogObserver observer) {
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
     * Injects a given value log into the stream of received value logs and passes it to all observers accordingly.
     *
     * @param valueLog The value log to inject
     */
    public void injectValueLog(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null,");
        }

        //Notify all registered observers about the value log
        notifyValueLogObservers(valueLog);
    }

    /**
     * Processes a recently received value log message by transforming it into a value log object and
     * notifying all registered observers about its arrival.
     *
     * @param message The arrived message
     * @param topic   The topic under which the message arrived
     */
    private void processValueLogMessage(JSONObject message, String topic) {
        //Sanity check
        if ((message == null) || message.equals(JSONObject.NULL)) {
            return;
        }

        //Catch errors during message processing to avoid crashes of the receiver
        try {
            //Record current time
            Instant time = ZonedDateTime.now().toInstant();

            //Create new value log object in which all the extracted fields can be stored
            ValueLog valueLog = new ValueLog();

            String componentType = message.getString(JSON_KEY_COMPONENT_TYPE);
            String componentID = message.getString(JSON_COMPONENT_ID);

            //Check component ID for validity
            if (!isComponentIDValid(componentID, componentType)) {
                System.out.println("Value with invalid component ID \"" + componentID + "\" received");
                return;
            }

            //Set value log fields
            valueLog.setTopic(topic);
            valueLog.setMessage(message.toString());
            valueLog.setTime(time);
            valueLog.setIdref(componentID);
            valueLog.setValue(message.getDouble(JSON_KEY_VALUE));
            valueLog.setComponent(componentType);

            //Notify all observers
            notifyValueLogObservers(valueLog);
        } catch (Exception e) {
            System.err.println("Value log processing failed: " + e.getMessage());
        }
    }

    /**
     * Notifies all observers that registered themselves to the ValueLogReceiver about the arrival of a new value log
     * message by passing the resulting ValueLog object.
     *
     * @param valueLog The value log to notify the observers about
     */
    private void notifyValueLogObservers(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null,");
        }

        //Notify all observers about the value log
        observerSet.forEach(o -> o.onValueReceived(valueLog));
    }

    /**
     * Checks and returns whether a given component ID is valid for a given component type by checking if
     * a component with such an ID exists in one of the component repositories.
     *
     * @param componentID   The component ID to check
     * @param componentType The type of the component to check
     * @return True, if the component ID is valid for this component type; false otherwise
     */
    private boolean isComponentIDValid(String componentID, String componentType) {
        //Sanity checks
        if ((componentID == null) || componentID.isEmpty() || (componentType == null) || (componentType.isEmpty())) {
            return false;
        }

        //Check component type
        if (componentType.toLowerCase().equals(new Actuator().getComponentTypeName())) {
            //Component is actuator, check if component exists
            return actuatorRepository.existsById(componentID);
        } else if (componentType.toLowerCase().equals(new Sensor().getComponentTypeName())) {
            //Component is sensor, check if component exists
            return sensorRepository.existsById(componentID);
        } else if (componentType.toLowerCase().equals(new MonitoringComponent().getComponentTypeName())) {
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

        //Given component type is unknown
        return false;
    }
}