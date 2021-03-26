
package de.ipvs.as.mbp.service.receiver;

import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.mqtt.MQTTService;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Background service that receives incoming MQTT value log messages that comply to certain topics. The service
 * implements the observer pattern which allows other components to register themselves to the ValueLogReceiver
 * and get notified in case a new value message arrives.
 */
@Service
@EnableScheduling
public class ValueLogReceiver {
    //Set of MQTT topics to subscribe to
    private static final String[] SUBSCRIBE_TOPICS = {"device/#", "sensor/#", "actuator/#", "monitoring/#"};

    //Set ob observers which want to be notified about incoming value logs
    private final Set<ValueLogReceiverObserver> observerSet;

    /**
     * Initializes the value logger service.
     *
     * @param mqttService                  The MQTTService for receiving MQTT messages (auto-wired)
     * @param actuatorRepository           Repository in which the actuators are stored (auto-wired)
     * @param sensorRepository             Repository in which the sensors are stored (auto-wired)
     * @param deviceRepository             Repository in which the devices are stored (auto-wired)
     * @param monitoringOperatorRepository Repository in which the monitoring operators are stored (auto-wired)
     */
    @Autowired
    public ValueLogReceiver(MQTTService mqttService, ActuatorRepository actuatorRepository,
                            SensorRepository sensorRepository, DeviceRepository deviceRepository,
                            MonitoringOperatorRepository monitoringOperatorRepository) {
        //Initialize set of observers
        observerSet = new HashSet<>();

        //Create MQTT callback handler
        ValueLogReceiverArrivalHandler handler = new ValueLogReceiverArrivalHandler(observerSet, actuatorRepository,
                sensorRepository, deviceRepository, monitoringOperatorRepository);

        //Register callback handler at MQTT service
        mqttService.setMqttCallback(handler);

        //Subscribe all topics that are relevant for receiving value logs
        for (String topic : SUBSCRIBE_TOPICS) {
            try {
                mqttService.subscribe(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
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
}