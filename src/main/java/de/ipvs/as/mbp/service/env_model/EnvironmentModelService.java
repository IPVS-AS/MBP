package de.ipvs.as.mbp.service.env_model;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.ComponentCreateEventHandler;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import de.ipvs.as.mbp.error.DeploymentException;
import de.ipvs.as.mbp.error.EnvironmentModelParseException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.repository.*;
import de.ipvs.as.mbp.service.UserService;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.env_model.events.EnvironmentModelEventService;
import de.ipvs.as.mbp.service.env_model.events.types.EntityStateEvent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Service for tasks related to environment models.
 */
@org.springframework.stereotype.Component
public class EnvironmentModelService {

    @Autowired
    private EnvironmentModelRepository environmentModelRepository;

    @Autowired
    private KeyPairRepository keyPairRepository;

    @Autowired
    private OperatorRepository operatorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ComponentCreateEventHandler componentCreateEventHandler;

    @Autowired
    private UserService userService;

    @Autowired
    private EnvironmentModelEventService eventService;

    @Autowired
    private DeployerDispatcher deployerDispatcher;

    //JSON key names
    private static final String MODEL_JSON_KEY_NODES = "nodes";
    private static final String MODEL_JSON_KEY_NODE_ID = "elementId";
    private static final String MODEL_JSON_KEY_NODE_TYPE = "nodeType";
    private static final String MODEL_JSON_KEY_NODE_COMPONENT_TYPE = "type";
    private static final String MODEL_JSON_KEY_NODE_DETAILS = "details";
    private static final String MODEL_JSON_KEY_NODE_DETAILS_NAME = "name";
    private static final String MODEL_JSON_KEY_DEVICE_IP = "ipAddress";
    private static final String MODEL_JSON_KEY_DEVICE_USERNAME = "username";
    private static final String MODEL_JSON_KEY_DEVICE_PASSWORD = "password";
    private static final String MODEL_JSON_KEY_DEVICE_KEYPAIR = "keyPair";
    private static final String MODEL_JSON_KEY_COMPONENT_OPERATOR = "operator";
    private static final String MODEL_JSON_KEY_CONNECTIONS = "connections";
    private static final String MODEL_JSON_KEY_CONNECTION_SOURCE = "sourceId";
    private static final String MODEL_JSON_KEY_CONNECTION_TARGET = "targetId";

    //Node types of the model
    private static final String MODEL_NODE_TYPE_DEVICE = "device";
    private static final String MODEL_NODE_TYPE_ACTUATOR = "actuator";
    private static final String MODEL_NODE_TYPE_SENSOR = "sensor";

    /**
     * Returns a map (node id -> entity state) holding the states for all registered entities of a given
     * environment model.
     *
     * @param model The environment model for which the entity states are supposed to be determined
     * @return The map (node id -> entity state) holding the states of all entities
     * @throws EnvironmentModelParseException In case the environment model could not be parsed
     */
    public Map<String, EntityState> retrieveEntityStates(EnvironmentModel model) throws EnvironmentModelParseException {
        //Sanity check
        if (model == null) {
            throw new EnvironmentModelParseException("Model must not be null.");
        }

        //Create new map (node id -> state) holding the states for each entity
        Map<String, EntityState> entityStates = new HashMap<>();

        //Get all registered entities (node id -> entity object)
        Map<String, UserEntity> registeredEntities = model.getEntityMap();

        //Iterate over all entities
        for (String nodeId : registeredEntities.keySet()) {
            //Get current entity object
            UserEntity entity = registeredEntities.get(nodeId);

            //Check if entity is a device
            if (entity instanceof Device) {
                //Device can only be registered
                entityStates.put(nodeId, EntityState.REGISTERED);
                continue;
            }

            //Skip entities that are no components
            if (!(entity instanceof Component)) {
                continue;
            }

            //Find suitable deployer component
            IDeployer deployer = deployerDispatcher.getDeployer();

            //Determine state of the component
            ComponentState componentState = deployer.retrieveComponentState((Component) entity);

            //Translate component state to entity state and add it to map
            switch (componentState) {
                case RUNNING:
                    entityStates.put(nodeId, EntityState.STARTED);
                    break;
                case DEPLOYED:
                    entityStates.put(nodeId, EntityState.DEPLOYED);
                default:
                    entityStates.put(nodeId, EntityState.REGISTERED);
            }
        }

        //Return result
        return entityStates;
    }

    /**
     * Unregisters (deletes) the entities of an environment model.
     *
     * @param model The model whose entities are supposed to be unregistered.
     * @throws EnvironmentModelParseException In case the environment model could not be parsed
     */
    public void unregisterEntities(EnvironmentModel model) throws EnvironmentModelParseException {
        //Sanity check
        if (model == null) {
            throw new EnvironmentModelParseException("Model must not be null.");
        }

        //Get entities that are associated with the model
        Map<String, UserEntity> entities = model.getEntityMap();

        //Santiy check
        if ((entities == null) || entities.isEmpty()) {
            return;
        }

        //Iterate over all entities and delete them
        for (Map.Entry<String, UserEntity> entry : entities.entrySet()) {
            //Get entity
            UserEntity entity = entry.getValue();

            //Check entity type
            if (entity instanceof Device) {
                deviceRepository.deleteById(entity.getId());
            } else if (entity instanceof Actuator) {
                actuatorRepository.deleteById(entity.getId());
            } else if (entity instanceof Sensor) {
                sensorRepository.deleteById(entity.getId());
            }
        }
    }

    /**
     * Registers the components of an environment model.
     *
     * @param model The model whose components are supposed to be registered
     * @throws EnvironmentModelParseException In case the environment model could not be parsed
     */
    public void registerComponents(EnvironmentModel model) throws EnvironmentModelParseException {
        //Sanity check
        if (model == null) {
            throw new EnvironmentModelParseException("Model must not be null.");
        }

        //Try to parse the model
        EnvironmentModelParseResult parseResult = parseModel(model);
        //Check if errors occurred while parsing
        checkParseResult(parseResult);

        //Unregister all components that have been previously registered
        unregisterEntities(model);

        //Get map (node id -> entity object) of registered entities and clear it
        Map<String, UserEntity> registeredEntities = model.getEntityMap();
        registeredEntities.clear();

        //Register all devices
        for (Map.Entry<String, Device> entry : parseResult.getDeviceMap().entrySet()) {
            //Get device
            Device device = entry.getValue();

            //Register device and obtain ID
            String deviceId = registerDevice(device);

            //Update device ID
            device.setId(deviceId);

            //Add to registered entity set
            registeredEntities.put(entry.getKey(), device);

            //Publish corresponding event
            publishEntityState(model, entry.getKey(), device, EntityState.REGISTERED);
        }

        //Get connections from parse result
        Map<Component, Device> connections = parseResult.getConnections();

        //Register all components for which a device is given
        for (Map.Entry<String, Component> entry : parseResult.getComponentMap().entrySet()) {
            //Get component
            Component component = entry.getValue();

            //Skip component if no connection exists for it
            if (!connections.containsKey(component)) {
                continue;
            }

            //Get associated device and set it
            Device targetDevice = connections.get(component);
            component.setDevice(targetDevice);

            //Register component and obtain ID
            String componentId;
            try {
                componentId = registerComponent(component);
            } catch (Exception e) {
                //Unregister all entities on failure
                unregisterEntities(model);
                throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "The environment model could not be created.");
            }

            //Set component ID
            component.setId(componentId);

            //Add to registered entity set
            registeredEntities.put(entry.getKey(), component);

            //Publish corresponding event
            publishEntityState(model, entry.getKey(), component, EntityState.REGISTERED);
        }

        //Update entity mapping of the model
        model.setEntityMap(registeredEntities);
        environmentModelRepository.save(model);

        //Check if something was registered
        if (registeredEntities.isEmpty()) {
            throw new MBPException(HttpStatus.INTERNAL_SERVER_ERROR, "The environment model could not be created.");
        }
    }

    /**
     * Deploys the components of an environment model.
     *
     * @param model The model whose components are supposed to be deployed
     * @throws DeploymentException In case of an error during deployment
     */
    public void deployComponents(EnvironmentModel model) throws DeploymentException {
        //Sanity check
        if (model == null) {
            throw new IllegalArgumentException("Model must not be null.");
        }

        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        //Map holding all occurred errors
        Map<String, String> deploymentErrors = new HashMap<>();

        //Get map of all entities
        Map<String, UserEntity> entityMap = model.getEntityMap();

        //Iterate over all entities
        for (String nodeId : entityMap.keySet()) {
            //Get entity
            UserEntity entity = entityMap.get(nodeId);

            //Check if entity is a component
            if (!(entity instanceof Component)) {
                continue;
            }

            //Cast entity to component
            Component component = (Component) entity;

            //Resolve current state of the component
            ComponentState componentState = deployer.retrieveComponentState(component);

            //Check if deployment is necessary and possible
            switch (componentState) {
                case DEPLOYED:
                    //Component is already deployed
                    publishEntityState(model, nodeId, component, EntityState.DEPLOYED);
                    continue;
                case RUNNING:
                    //Component is already running
                    publishEntityState(model, nodeId, component, EntityState.STARTED);
                    continue;
                case UNKNOWN:
                case NOT_READY:
                    //Impossible to deploy component
                    publishEntityState(model, nodeId, component, EntityState.REGISTERED);

                    //Update deployment error map
                    deploymentErrors.put(nodeId, "Impossible to deploy, device is not available.");
                    continue;
                default:
                    break;
            }

            //Try to deploy component
            try {
                deployer.deployComponent(component);

                //Publish update event
                publishEntityState(model, nodeId, component, EntityState.DEPLOYED);
            } catch (Exception e) {
                //Remember error
                deploymentErrors.put(nodeId, "Deployment failed unexpectedly.");
            }
        }

        if (!deploymentErrors.isEmpty()) {
            throw new DeploymentException("Environment model could not be deployed.", deploymentErrors);
        }
    }

    /**
     * Undeploys the components of an environment model.
     *
     * @param model The model whose components are supposed to be undeployed
     */
    public void undeployComponents(EnvironmentModel model) {
        //Sanity check
        if (model == null) {
            throw new IllegalArgumentException("Model must not be null.");
        }

        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        //Map holding all occurred errors
        Map<String, String> undeploymentErrors = new HashMap<>();

        //Get map of all entities
        Map<String, UserEntity> entityMap = model.getEntityMap();

        //Iterate over all entities
        for (String nodeId : entityMap.keySet()) {
            //Get entity
            UserEntity entity = entityMap.get(nodeId);

            //Check if entity is a component
            if (!(entity instanceof Component)) {
                continue;
            }

            //Cast entity to component
            Component component = (Component) entity;

            //Resolve current state of the component
            ComponentState componentState = deployer.retrieveComponentState(component);

            //Check if undeployment is necessary and possible
            switch (componentState) {
                case READY:
                case UNKNOWN:
                case NOT_READY:
                    //Impossible to undeploy component
                    publishEntityState(model, nodeId, component, EntityState.REGISTERED);
                    continue;
                case DEPLOYED:
                    break;
                default:
                    break;
            }

            //Try to undeploy component
            try {
                deployer.undeployComponent(component);

                //Publish update event
                publishEntityState(model, nodeId, component, EntityState.REGISTERED);
            } catch (Exception e) {
                //Remember error
                undeploymentErrors.put(nodeId, "Undeployment failed unexpectedly.");
            }
        }

        if (!undeploymentErrors.isEmpty()) {
            throw new DeploymentException("Environment model could not be undeployed.", undeploymentErrors);
        }
    }

    /**
     * Starts the components of an environment model.
     *
     * @param model The model whose components are supposed to be started
     */
    public void startComponents(EnvironmentModel model) {
        //Sanity check
        if (model == null) {
            throw new IllegalArgumentException("Model must not be null.");
        }

        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        //Map holding all occurred errors
        Map<String, String> startErrors = new HashMap<>();

        //Get map of all entities
        Map<String, UserEntity> entityMap = model.getEntityMap();

        //Iterate over all entities
        for (String nodeId : entityMap.keySet()) {
            //Get entity
            UserEntity entity = entityMap.get(nodeId);

            //Check if entity is a component
            if (!(entity instanceof Component)) {
                continue;
            }

            //Cast entity to component
            Component component = (Component) entity;

            //Resolve current state of the component
            ComponentState componentState = deployer.retrieveComponentState(component);

            //Check if starting is necessary and possible
            switch (componentState) {
                case RUNNING:
                    //Component is running
                    publishEntityState(model, nodeId, component, EntityState.STARTED);
                    continue;
                case READY:
                case UNKNOWN:
                case NOT_READY:
                    //Impossible to undeploy component
                    publishEntityState(model, nodeId, component, EntityState.REGISTERED);

                    //Update deployment error map
                    startErrors.put(nodeId, "Impossible to start, device is not available.");
                    continue;
                case DEPLOYED:
                    break;
                default:
                    break;
            }

            //Try to start component
            try {
                deployer.startComponent(component, new ArrayList<>());

                //Publish update event
                publishEntityState(model, nodeId, component, EntityState.STARTED);
            } catch (Exception e) {
                //Remember error
                startErrors.put(nodeId, "Starting failed unexpectedly.");
            }
        }

        if (!startErrors.isEmpty()) {
            throw new DeploymentException("One or more components of the environment model could not be started.", startErrors);
        }
    }

    /**
     * Stops the components of an environment model.
     *
     * @param model The model whose components are supposed to be stopped
     * @throws DeploymentException In case of a failure on undeployment
     */
    public void stopComponents(EnvironmentModel model) throws DeploymentException {
        //Sanity check
        if (model == null) {
            throw new IllegalArgumentException("Model must not be null.");
        }

        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        //Map holding all occurred errors
        Map<String, String> stopErrors = new HashMap<>();

        //Get map of all entities
        Map<String, UserEntity> entityMap = model.getEntityMap();

        //Iterate over all entities
        for (String nodeId : entityMap.keySet()) {
            //Get entity
            UserEntity entity = entityMap.get(nodeId);

            //Check if entity is a component
            if (!(entity instanceof Component)) {
                continue;
            }

            //Cast entity to component
            Component component = (Component) entity;

            //Resolve current state of the component
            ComponentState componentState = deployer.retrieveComponentState(component);

            //Check if stopping is necessary and possible
            switch (componentState) {
                case DEPLOYED:
                    //Component is not running
                    publishEntityState(model, nodeId, component, EntityState.DEPLOYED);
                    continue;
                case READY:
                case UNKNOWN:
                case NOT_READY:
                    //Impossible to undeploy component
                    publishEntityState(model, nodeId, component, EntityState.REGISTERED);
                    continue;
                default:
                    break;
            }

            //Try to stop component
            try {
                deployer.stopComponent(component);

                //Publish update event
                publishEntityState(model, nodeId, component, EntityState.DEPLOYED);
            } catch (Exception e) {
                //Remember error
                stopErrors.put(nodeId, "Stopping failed unexpectedly.");
            }
        }

        if (!stopErrors.isEmpty()) {
            throw new DeploymentException("One or more components of the environment model could not be stopped.", stopErrors);
        }
    }

    /**
     * Converts a given parse result object into an action response object by transforming all parse errors.
     *
     * @param parseResult The parse result object to transform
     * @throws EnvironmentModelParseException In case the environment model could not be parsed
     */
    private void checkParseResult(EnvironmentModelParseResult parseResult) throws EnvironmentModelParseException {
        //Sanity check
        if (parseResult == null) {
            throw new MBPException(HttpStatus.BAD_REQUEST, "Could not parse model.");
        }

        //Check for errors
        if (!parseResult.hasErrors()) {
            //No errors
            return;
        }

        //Get parse errors
        Map<UserEntity, Errors> parseErrors = parseResult.getErrors();

        //Iterate over all parse errors
        for (UserEntity entity : parseErrors.keySet()) {
            //Get iterator for errors of this entity
            Iterator<FieldError> errorIterator = parseErrors.get(entity).getFieldErrors().iterator();

            //Get node ID of the entity
            String nodeId = null;

            //Check for entity type
            if (entity instanceof Device) {
                nodeId = parseResult.getNodeIdForDevice((Device) entity);
            } else if (entity instanceof Component) {
                nodeId = parseResult.getNodeIdForComponent((Component) entity);
            }

            //Check if node id was found
            if (nodeId == null) {
                //Skip error
                continue;
            }

            //Iterate over the errors
            EnvironmentModelParseException e = new EnvironmentModelParseException("One or more errors occurred while parsing the environment model.");
            while (errorIterator.hasNext()) {
                //Get next error
                FieldError error = errorIterator.next();

                //Put field name together
                String fieldName = nodeId + "." + error.getField();

                //Add error to action response
                e.addParseError(fieldName, error.getDefaultMessage());
            }
            throw e;
        }


    }

    /**
     * Publishes the state of an entity to all subscribers of a model.
     *
     * @param model       The model to which the entity belongs
     * @param nodeId      The node ID of the affected entity within the model
     * @param entity      The entity whose state is supposed to be updated
     * @param entityState The new state of the entity
     */
    private void publishEntityState(EnvironmentModel model, String nodeId, UserEntity entity, EntityState entityState) {
        //Create new event
        EntityStateEvent updateEvent = new EntityStateEvent(nodeId, entity, entityState);

        //Publish event
        eventService.publishEvent(model.getId(), updateEvent);
    }

    private EnvironmentModelParseResult parseModel(EnvironmentModel model) throws EnvironmentModelParseException {
        try {
            //Sanity check
            if (model == null) {
                throw new IllegalArgumentException("Model must not be null.");
            }

            //Create new result object
            EnvironmentModelParseResult result = new EnvironmentModelParseResult();

            //Read JSON from model
            JSONObject jsonObject = new JSONObject(model.getModelJSON());

            //Get all nodes
            JSONArray nodes = jsonObject.getJSONArray(MODEL_JSON_KEY_NODES);

            //Create entity mapping (node id -> entity object) which is required for parsing connections
            Map<String, UserEntity> entityMapping = new HashMap<>();

            //Iterate over all nodes to get all devices
            for (int i = 0; i < nodes.length(); i++) {
                //Get current node object
                JSONObject nodeObject = nodes.getJSONObject(i);

                //Ensure that details are available
                if (!nodeObject.has(MODEL_JSON_KEY_NODE_DETAILS)) {
                    continue;
                }

                //Get node ID
                String nodeID = nodeObject.getString(MODEL_JSON_KEY_NODE_ID);

                //Get node type
                String nodeType = nodeObject.optString(MODEL_JSON_KEY_NODE_TYPE, "");

                //Unmarshalled user entity
                UserEntity entity;

                //Validation errors
                Errors errors;

                //Differentiate between node types
                if (nodeType.equals(MODEL_NODE_TYPE_DEVICE)) {
                    //Create device object from node object
                    Device device = unmarshallDevice(nodeObject);
                    entity = device;

                    //Add device to map
                    entityMapping.put(nodeID, device);

                    //Add device to result
                    result.addDevice(device, nodeID);

                    //Validate device
                    errors = new BeanPropertyBindingResult(device, "device");
                    //deviceCreateValidator.validate(device, errors); TODO
                } else if (nodeType.equals(MODEL_NODE_TYPE_ACTUATOR) || nodeType.equals(MODEL_NODE_TYPE_SENSOR)) {
                    //Create device object from node object
                    Component component = unmarshallComponent(nodeObject);
                    entity = component;

                    //Add component to map
                    entityMapping.put(nodeID, component);

                    //Add component to result
                    result.addComponent(component, nodeID);

                    //Validate component
                    errors = new BeanPropertyBindingResult(component, "component");
                    if (nodeType.equals(MODEL_NODE_TYPE_ACTUATOR)) {
                        //componentCreateValid3ator.validate(component, errors); TODO
                    } else {
                        //sensorValidator.validate(component, errors); TODO
                    }
                } else {
                    //Ignore other node types
                    continue;
                }

                //Set entity owner
                User owner = userService.getLoggedInUser();
                entity.setOwner(owner);

                //Mark entity as modelled
                entity.setEnvironmentModel(model);

                //CHeck for errors
                if (errors.hasErrors()) {
                    result.addErrors(entity, errors);
                }
            }

            //Return already if there were errors
            if (result.hasErrors()) {
                return result;
            }

            //Get all connections
            JSONArray connections = jsonObject.optJSONArray(MODEL_JSON_KEY_CONNECTIONS);

            //Iterate over the connections and parse them one by one
            for (int i = 0; i < connections.length(); i++) {
                //Get current connection object
                JSONObject connectionObject = connections.getJSONObject(i);

                //Get source and target ID
                String sourceId = connectionObject.optString(MODEL_JSON_KEY_CONNECTION_SOURCE, "");
                String targetId = connectionObject.optString(MODEL_JSON_KEY_CONNECTION_TARGET, "");

                //Only remember connection if source device and target component exist
                if (entityMapping.containsKey(sourceId) && entityMapping.containsKey(targetId)) {
                    //Get source and target entity
                    UserEntity sourceEntity = entityMapping.get(sourceId);
                    UserEntity targetEntity = entityMapping.get(targetId);

                    //Skip connection if entities are of wrong type
                    if ((!(sourceEntity instanceof Device) || (!(targetEntity instanceof Component)))) {
                        continue;
                    }

                    //Add connection
                    result.addConnection((Device) sourceEntity, (Component) targetEntity);
                }
            }

            return result;
        } catch (JSONException e) {
            throw new EnvironmentModelParseException("Environment model could not be parsed.");
        }
    }

    /**
     * Registers a device in the device repository and returns its new ID.
     *
     * @param device The device to register
     * @return The generated ID of the device
     */
    private String registerDevice(Device device) {
        //Sanity check
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null.");
        }

        //Insert device into repository
        Device savedEntity = deviceRepository.insert(device);

        //Get new ID and return it
        return savedEntity.getId();
    }

    /**
     * Registers a component in its dedicated repository and returns its new ID.
     *
     * @param component The component to register
     * @return The generated ID of the component
     */
    private String registerComponent(Component component) {
        //Sanity check
        if (component == null) {
            throw new IllegalArgumentException("Component must not be null.");
        }

        //Store registered Component
        Component registeredComponent;

        //Insert component into its repository and return the new id
        if (component instanceof Actuator) {
            registeredComponent = actuatorRepository.insert((Actuator) component);
        } else {
            registeredComponent = sensorRepository.insert((Sensor) component);
        }

        //Call creation handler for this component
        componentCreateEventHandler.onCreate(registeredComponent);

        return registeredComponent.getId();
    }


    private Device unmarshallDevice(JSONObject nodeObject) throws JSONException {
        //Sanity check
        if (nodeObject == null) {
            throw new IllegalArgumentException("Node object must not be null.");
        }

        //Get node details containing all relevant properties
        JSONObject deviceDetails = nodeObject.getJSONObject(MODEL_JSON_KEY_NODE_DETAILS);

        //Create new device object
        Device device = new Device();

        //Enrich device for details
        device.setName(deviceDetails.getString(MODEL_JSON_KEY_NODE_DETAILS_NAME));
        device.setComponentType(nodeObject.getString(MODEL_JSON_KEY_NODE_COMPONENT_TYPE));
        device.setIpAddress(deviceDetails.optString(MODEL_JSON_KEY_DEVICE_IP, ""));
        device.setUsername(deviceDetails.optString(MODEL_JSON_KEY_DEVICE_USERNAME, ""));
        device.setPassword(deviceDetails.optString(MODEL_JSON_KEY_DEVICE_PASSWORD, ""));

        //Check if a key pair was provided
        if (!deviceDetails.isNull(MODEL_JSON_KEY_DEVICE_KEYPAIR)) {
            //Get key pair from repository and set it
            KeyPair keyPair = keyPairRepository.findById(deviceDetails.optString(MODEL_JSON_KEY_DEVICE_KEYPAIR)).get();
            device.setKeyPair(keyPair);
        }

        //Return final device object
        return device;
    }

    private Component unmarshallComponent(JSONObject nodeObject) throws JSONException {
        //Sanity check
        if (nodeObject == null) {
            throw new IllegalArgumentException("Node object must not be null.");
        }

        //Get node type
        String nodeType = nodeObject.getString(MODEL_JSON_KEY_NODE_TYPE);

        //Get node details containing all relevant properties
        JSONObject componentDetails = nodeObject.getJSONObject(MODEL_JSON_KEY_NODE_DETAILS);

        //Create new component object (either actuator or sensor)
        Component component = nodeType.equals(MODEL_NODE_TYPE_ACTUATOR) ? new Actuator() : new Sensor();

        //Enrich component for details
        component.setName(componentDetails.getString(MODEL_JSON_KEY_NODE_DETAILS_NAME));
        component.setComponentType(nodeObject.getString(MODEL_JSON_KEY_NODE_COMPONENT_TYPE));

        //Find operator from repository and set it
        Operator operator = operatorRepository.findById(componentDetails.optString(MODEL_JSON_KEY_COMPONENT_OPERATOR)).get();
        component.setOperator(operator);

        //Set a fake device for passing validation
        component.setDevice(new Device());

        return component;
    }
}
