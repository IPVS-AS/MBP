package de.ipvs.as.mbp.service.cep.trigger;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.receiver.ValueLogObserver;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.service.cep.engine.core.CEPEngine;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEventType;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;
import de.ipvs.as.mbp.service.cep.engine.core.exceptions.EventNotRegisteredException;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQuery;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQueryValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This service provides means for registering rule triggers with callbacks at the CEP engine. Furthermore,
 * it takes care about registering event types for different the different components entities at the CEP engine
 * and works as a observer for the received value logs.
 */
@Service
public class CEPTriggerService implements ValueLogObserver {

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
            throw new IllegalArgumentException("Rule trigger must not be null.");
        }

        //Extract query
        String query = ruleTrigger.getQuery();

        //Check if query starts with a select clause
        if (!query.trim().startsWith("SELECT")) {
            return new CEPQueryValidation(query, false, "Query must start with a \"SELECT\" clause.");
        }

        //Validity check
        return engine.validateQuery(query);
    }

    /**
     * Registers all components that are currently stored in their repositories (auto-wired)
     * as event types at the CEP engine.
     *
     * @param actuatorRepository          The actuator repository
     * @param sensorRepository            The sensor repository
     * @param monitoringOperatorRepository The monitoring adapter repository
     * @param deviceRepository            The device repository
     */
    @Autowired
    private void registerAvailableEventTypes(ActuatorRepository actuatorRepository, SensorRepository sensorRepository,
                                             MonitoringOperatorRepository monitoringOperatorRepository,
                                             DeviceRepository deviceRepository) {
        //Create set of available components
        Set<Component> componentSet = new HashSet<>();

        //Add actuators and sensors
        componentSet.addAll(actuatorRepository.findAll());
        componentSet.addAll(sensorRepository.findAll());

        //Get all monitoring adapters and devices
        List<MonitoringOperator> monitoringAdapters = monitoringOperatorRepository.findAll();
        List<Device> deviceAdapters = deviceRepository.findAll();

        //Iterate over all monitoring adapters and devices and create monitoring components for them
        for (MonitoringOperator monitoringAdapter : monitoringAdapters) {
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