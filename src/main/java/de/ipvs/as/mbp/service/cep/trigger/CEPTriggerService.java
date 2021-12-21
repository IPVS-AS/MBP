package de.ipvs.as.mbp.service.cep.trigger;

import java.util.*;

import com.jayway.jsonpath.JsonPath;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.data_model.DataModelDataType;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTree;
import de.ipvs.as.mbp.domain.data_model.treelogic.DataModelTreeNode;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiverObserver;
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
public class CEPTriggerService implements ValueLogReceiverObserver {

    // The data model tree cache to receive the data model tree of the respective component
    @Autowired
    private DataModelTreeCache dataModelTreeCache;

    // To store event type information in the parser service (to convert complex ValueLog data to CEPEvent representation)
    @Autowired
    private CEPValueLogParser cepValueLogParser;

    // To store incoming ValueLogs temporarily for the case that the ValueLog was not already saved in the MongoDB
    private final CEPValueLogCache cepValueLogCache;

    //The CEP engine instance to use
    private final CEPEngine engine;

    /**
     * Creates and initializes the CEP trigger service by passing a certain rule engine and a value log receiver
     * instance (autowired).
     *
     * @param engine           The rule engine to use
     * @param valueLogReceiver The value log receiver instance to use
     */
    @Autowired
    CEPTriggerService(CEPEngine engine, CEPValueLogCache cepValueLogCache, ValueLogReceiver valueLogReceiver) {
        this.engine = engine;
        this.cepValueLogCache = cepValueLogCache;

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
        // Pass the valueLog to the cache to have later access to it, even if it is not already written in the mongoDB
        cepValueLogCache.addValueLog(valueLog);

        // Let the parser parse the valuelog
        Map<String, Object> parsedLog = cepValueLogParser.parseValueLog(valueLog,
                CEPValueLogEvent.generateEventTypeName(valueLog.getIdref(), valueLog.getComponent())
        );


        //Create event from value log
        CEPValueLogEvent valueLogEvent = new CEPValueLogEvent(valueLog, parsedLog);

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

        // Get the data model tree of the component from the data model tree cache
        DataModelTree dataModel = dataModelTreeCache.getDataModelOfComponent(component.getId());

        // Extra case for monitoring operators and devices for which no data model exists
        if (dataModel == null) {
            eventType.addField("value", CEPPrimitiveDataTypes.DOUBLE);
            eventType.addField("time", CEPPrimitiveDataTypes.LONG);
            //Register event type
            engine.registerEventType(eventType);
            return;
        }

        // Get the leaf nodes as these should be the only options for available CEP event fields.
        List<DataModelTreeNode> leafNodes = dataModel.getLeafNodes();

        Set<CEPValueLogParseInstruction> pathInstructions = new HashSet<>();

        /*
         * Retrieve all jsonPaths to this leaf node (including all array index combinations) and add for each
         * a corresponding named and properly typed field to the event type.
         */
        for (DataModelTreeNode node : leafNodes) {

            // All path variants for the current leaf node
            List<String> paths = new ArrayList<>();

            // Check if the leaf has arrays as parents, if yes then all array index combinations must be added
            // to the event type
            String[] splittedPath = node.getInternPathToNode().split("#");

            // Check if the leaf node has no arrays as ancestor
            if (splittedPath.length == 1) {
                paths.add(node.getJsonPathToNode().getPath());
            }

            // All array dimensions of the jsonPath sorted by occurrence position (left to right)
            List<Integer> arrayDimensions = new ArrayList<>();

            // Fragments of the jsonPath without the indices
            List<String> splittedPathFragmentsWithOutIndices = new ArrayList<>();

            // All index possibilities for the jsonPath. For each nested list a event type field will be added
            List<List<Integer>> arrayIndicesPerPathToBuild = new ArrayList<>();

            // Check if the array has arrays as ancestor, if yes proceed with considering the different index combinations
            if (splittedPath.length > 1) {

                // Retrieve the dimensions of the array and string fragments of the json path without indices
                for (int i = 0; i < splittedPath.length; i++) {
                    if (i % 2 == 1) {
                        // The fragment is an array dimension
                        arrayDimensions.add(Integer.parseInt(splittedPath[i]));
                    } else {
                        // The fragment is a json path fragment
                        splittedPathFragmentsWithOutIndices.add(splittedPath[i]);
                    }
                }

                // Get all combinations of array indices for the current path
                arrayIndicesPerPathToBuild = this.getAllIndexCombinationsForJsonPath(arrayDimensions);

                // Build the jsonPath string per possible array index combination
                for (List<Integer> pathIndicesToAdd : arrayIndicesPerPathToBuild) {
                    StringBuilder pathBuilder = new StringBuilder("");

                    // Iterate over all indices which should be added to the current jsonPath
                    for (int i = 0; i < splittedPathFragmentsWithOutIndices.size(); i++) {
                        // Append the jsonPath fragment without array index
                        pathBuilder.append(splittedPathFragmentsWithOutIndices.get(i));

                        // If string is not the last item add the next array index to the path
                        if (i != splittedPathFragmentsWithOutIndices.size() - 1) {
                            pathBuilder.append(pathIndicesToAdd.get(i));
                        }
                    }
                    paths.add(pathBuilder.toString());
                }
            }

            // Add all json path options for the current leaf node to the event type
            DataModelDataType typeOfNode = node.getType();
            for (String path : paths) {
                JsonPath jsonPath = JsonPath.compile(path);
                // Remove the $ from the json path
                path = path.substring(1);
                // No arrays are in the path --> just add the path as field
                if (DataModelDataType.hasCepPrimitiveDataType(typeOfNode)) {
                    eventType.addField(path, typeOfNode.getCepType());
                } else {
                    // Special cases for special IoT data types like Date and Binary for which no explicit mapping is defined
                    if (typeOfNode == DataModelDataType.DATE) {
                        eventType.addField(path, CEPPrimitiveDataTypes.LONG);
                    } else if (typeOfNode == DataModelDataType.BINARY) {
                        eventType.addField(path, CEPPrimitiveDataTypes.STRING);
                    }
                }
                pathInstructions.add(new CEPValueLogParseInstruction(path, jsonPath, typeOfNode));
            }

        }

        // Add a time data field as default event field
        eventType.addField("time", CEPPrimitiveDataTypes.LONG);

        // Add the parseInstructions to the parser cache
        cepValueLogParser.addInstructionsForEventType(eventType.getName(), pathInstructions);

        //Register event type
        engine.registerEventType(eventType);
        // TODO Is it somehow foreseen to remove event types again form the engine?
        //      (same applies then also to the cepValueLogParser
    }

    /**
     * Computes all combinations of array indices by a given list of maximal array dimensions.
     *
     * @param dimensionsOfPath A list of array dimensions, sorted by the array occurrence.
     * @return A nested list of all array index combinations within the given array dimensions.
     */
    private List<List<Integer>> getAllIndexCombinationsForJsonPath(List<Integer> dimensionsOfPath) {
        // All index possibilities for the jsonPath. For each nested list a event type field will be added
        List<List<Integer>> arrayIndicesPerPathToBuild = new ArrayList<>();

        // Retrieve all array index combinations and fill the arrayIndicesPerPathToBuild list
        for (Integer dimension : dimensionsOfPath) {
            if (arrayIndicesPerPathToBuild.size() <= 0) {
                for (int ind = 0; ind < dimension; ind++) {
                    List<Integer> indPerPath = new ArrayList<>();
                    indPerPath.add(ind);
                    arrayIndicesPerPathToBuild.add(indPerPath);
                }
            } else {
                List<List<Integer>> pathIndicesToAdd = new ArrayList<>();
                List<List<Integer>> pathIndicesToRemove = new ArrayList<>();
                for (int ind = 0; ind < dimension; ind++) {
                    for (List<Integer> indPerPath : arrayIndicesPerPathToBuild) {
                        List<Integer> tmp = new ArrayList<>();
                        for (Integer toAdd : indPerPath) {
                            tmp.add(toAdd);
                        }
                        tmp.add(ind);
                        pathIndicesToAdd.add(tmp);
                        if (!pathIndicesToRemove.contains(indPerPath)) {
                            pathIndicesToRemove.add(indPerPath);
                        }
                    }
                }
                arrayIndicesPerPathToBuild.addAll(pathIndicesToAdd);
                arrayIndicesPerPathToBuild.removeAll(pathIndicesToRemove);
            }
        }

        return arrayIndicesPerPathToBuild;
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
     * @param actuatorRepository           The actuator repository
     * @param sensorRepository             The sensor repository
     * @param monitoringOperatorRepository The monitoring adapter repository
     * @param deviceRepository             The device repository
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