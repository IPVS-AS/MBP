package org.citopt.connde.service.cep.trigger;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.cep.engine.core.CEPEngine;
import org.citopt.connde.service.cep.engine.core.events.CEPEventType;
import org.citopt.connde.service.cep.engine.core.events.CEPPrimitiveDataTypes;
import org.citopt.connde.service.cep.engine.core.exceptions.EventNotRegisteredException;
import org.citopt.connde.service.cep.engine.core.queries.CEPQuery;
import org.citopt.connde.service.cep.engine.core.queries.CEPQueryValidation;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.citopt.connde.service.receiver.ValueLogReceiverObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This service provides means for registering rule triggers with callbacks at the CEP engine. Furthermore,
 * it takes care about registering event types for different the different components entities at the CEP engine
 * and works as a observer for the received value logs.
 */
@Service
public class CEPTriggerService implements ValueLogReceiverObserver {

    //The CEP engine instance to use
    private CEPEngine engine;

    /**
     * Creates and initializes the CEP trigger service by passing a certain rule engine and a value log receiver
     * instance (autowired).
     *
     * @param engine           The rule engine to use
     * @param valueLogReceiver The value log receiver instance to use
     */
    @Autowired
    private CEPTriggerService(CEPEngine engine, ValueLogReceiver valueLogReceiver) {
        this.engine = engine;

        //Register as observer at the ValueLogReceiver
        valueLogReceiver.registerObserver(this);
    }

    /**
     * Registers a certain rule trigger at the CEP engine with a rule trigger callback object which is called
     * in case the trigger fires.
     *
     * @param ruleTrigger The rule trigger to register
     */
    public void registerTrigger(RuleTrigger ruleTrigger, RuleTriggerCallback callback) {
        //Sanity check
        if (ruleTrigger == null) {
            throw new IllegalArgumentException("Rule trigger must not be null.");
        }

        //Generate query name
        String name = getQueryNameFromTrigger(ruleTrigger);

        //Create query
        CEPQuery query = engine.createQuery(name, ruleTrigger.getQuery());

        //Set query subscriber
        query.setSubscriber(output -> {
            //Execute rule trigger callback method
            callback.onTriggerFired(ruleTrigger, output);
        });
    }

    /**
     * Unregisters a certain trigger from the CEP engine.
     *
     * @param ruleTrigger The rule trigger to unregister
     */
    public void unregisterTrigger(RuleTrigger ruleTrigger) {
        //Sanity check
        if (ruleTrigger == null) {
            throw new IllegalArgumentException("Rule trigger must not be null.");
        }

        //Generate query name
        String name = getQueryNameFromTrigger(ruleTrigger);

        //Get query with this name from engine
        CEPQuery query = engine.getQueryByName(name);

        //Sanity check
        if (query == null) {
            return;
        }

        //Unregister query
        query.disable();
        query.unregister();
    }

    /**
     * Called in case a new value message arrives at the ValueLogReceiver. The transformed message is passed
     * as value log.
     *
     * @param valueLog The corresponding value log that arrived
     */
    @Override
    public void onValueReceived(ValueLog valueLog) {
        //Create event from value log
        CEPValueLogEvent valueLogEvent = new CEPValueLogEvent(valueLog);

        //Send event to engine
        try {
            engine.sendEvent(valueLogEvent);
        } catch (EventNotRegisteredException e) {
            System.err.println("Event not registered: " + e.getMessage());
        }
    }

    /**
     * Generates a query name from a given rule trigger.
     *
     * @param ruleTrigger The rule trigger to use
     * @return The generated name for the rule trigger
     */
    private String getQueryNameFromTrigger(RuleTrigger ruleTrigger) {
        //Sanity check
        if (ruleTrigger == null) {
            throw new IllegalArgumentException("Rule trigger must not be null.");
        }

        return "trigger-" + ruleTrigger.getId();
    }

    /**
     * Registers a separate event type for a certain component at the CEP engine so that derived events for this
     * component may be sent to the CEP engine in the future.
     *
     * @param component The component for which the event type is supposed to be registered
     */
    public void registerComponentEventType(Component component) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Get generated event type name for this component
        String eventName = CEPValueLogEvent.generateEventTypeName(component.getId(), component.getComponentTypeName());

        //Create new event type (a "template" for such events) for this component
        CEPEventType eventType = new CEPEventType(eventName);

        //Add fields to this event type that all derived events need to implement
        eventType.addField("value", CEPPrimitiveDataTypes.DOUBLE);
        eventType.addField("time", CEPPrimitiveDataTypes.LONG);

        //Register event type
        engine.registerEventType(eventType);
    }

    /**
     * Validates the query string of a given rule trigger by checking whether it is
     * syntactically and semantically valid.
     *
     * @param ruleTrigger The rule trigger to validate
     * @return The result of the validation wrapped in a validation object
     */
    public CEPQueryValidation isValidTriggerQuery(RuleTrigger ruleTrigger) {
        //Sanity check
        if (ruleTrigger == null) {
            System.out.println("Rule trigger must not be null.");
        }

        //Extract query
        String query = ruleTrigger.getQuery();

        //Validity check
        return engine.validateQuery(query);
    }

    /**
     * Registers all components that are currently stored in their repositories (auto-wired)
     * as event types at the CEP engine.
     *
     * @param actuatorRepository          The actuator repository
     * @param sensorRepository            The sensor repository
     * @param monitoringAdapterRepository The monitoring adapter repository
     * @param deviceRepository            The device repository
     */
    @Autowired
    private void registerAvailableEventTypes(ActuatorRepository actuatorRepository, SensorRepository sensorRepository,
                                             MonitoringAdapterRepository monitoringAdapterRepository,
                                             DeviceRepository deviceRepository) {
        //Create set of available components
        Set<Component> componentSet = new HashSet<>();

        //Add actuators and sensors
        componentSet.addAll(actuatorRepository.findAll());
        componentSet.addAll(sensorRepository.findAll());

        //Get all monitoring adapters and devices
        List<MonitoringAdapter> monitoringAdapters = monitoringAdapterRepository.findAll();
        List<Device> deviceAdapters = deviceRepository.findAll();

        //Iterate over all monitoring adapters and devices and create monitoring components for them
        for (MonitoringAdapter monitoringAdapter : monitoringAdapters) {
            for (Device device : deviceAdapters) {
                //Create monitoring component
                MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);

                //Add component to component set
                componentSet.add(monitoringComponent);
            }
        }

        //Iterate over all stored components and register the dedicated event types
        for (Component component : componentSet) {
            registerComponentEventType(component);
        }
    }
}