package org.citopt.connde.service.env_model;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.component.*;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.device.DeviceValidator;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for tasks related to environment models.
 */
@org.springframework.stereotype.Component
public class EnvironmentModelService {

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceValidator deviceValidator;

    @Autowired
    private ActuatorValidator actuatorValidator;

    @Autowired
    private SensorValidator sensorValidator;

    @Autowired
    private AdapterRepository adapterRepository;

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
    private static final String MODEL_JSON_KEY_DEVICE_RSAKEY = "rsaKey";
    private static final String MODEL_JSON_KEY_COMPONENT_ADAPTER = "adapter";
    private static final String MODEL_JSON_KEY_CONNECTIONS = "connections";

    //Node types of the model
    private static final String MODEL_NODE_TYPE_DEVICE = "device";
    private static final String MODEL_NODE_TYPE_ACTUATOR = "actuator";
    private static final String MODEL_NODE_TYPE_SENSOR = "sensor";

    /**
     * Registers the components of an environment model.
     *
     * @param model The model whose components are supposed to be registered
     * @return An action response containing the result of the registration
     */
    public ActionResponse registerComponents(EnvironmentModel model) {
        //Sanity check
        if (model == null) {
            throw new IllegalArgumentException("Model must not be null.");
        }

        //Try to parse the model
        EnvironmentModelParseResult parseResult;
        try {
            parseResult = parseModel(model);
        } catch (JSONException e) {
            //Parsing failed
            return new ActionResponse(false, "Could not parse model.");
        }

        return new ActionResponse(true);
    }

    private EnvironmentModelParseResult parseModel(EnvironmentModel model) throws JSONException {
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

        //Create entity maps (node id -> entity object)
        Map<String, Device> deviceMap = new HashMap<>();
        Map<String, Component> componentMap = new HashMap<>();

        //Iterate over all nodes to get all devices
        for (int i = 0; i < nodes.length(); i++) {
            //Get current node object
            JSONObject nodeObject = nodes.getJSONObject(i);

            //Get node ID
            String nodeID = nodeObject.getString(MODEL_JSON_KEY_NODE_ID);

            //Get node type
            String nodeType = nodeObject.optString(MODEL_JSON_KEY_NODE_TYPE, "");

            //Unmarshalled user entity
            UserEntity entity = null;

            //Validation errors
            Errors errors = null;

            //Differentiate between node types
            if (nodeType.equals(MODEL_NODE_TYPE_DEVICE)) {
                //Create device object from node object
                Device device = unmarshallDevice(nodeObject);
                entity = device;

                //Add device to map
                deviceMap.put(nodeID, device);

                //Add device to result
                result.addDevice(device);

                //Validate device
                errors = new BeanPropertyBindingResult(device, "device");
                deviceValidator.validate(device, errors);
            } else if (nodeType.equals(MODEL_NODE_TYPE_ACTUATOR) || nodeType.equals(MODEL_NODE_TYPE_SENSOR)) {
                //Create device object from node object
                Component component = unmarshallComponent(nodeObject);
                entity = component;

                //Add device to map
                componentMap.put(nodeID, component);

                //Add component to result
                result.addComponent(component);

                //Validate component
                errors = new BeanPropertyBindingResult(component, "component");
                if (nodeType.equals(MODEL_NODE_TYPE_ACTUATOR)) {
                    actuatorValidator.validate(component, errors);
                } else {
                    sensorValidator.validate(component, errors);
                }
            } else {
                //Ignore other node types
                continue;
            }

            //Set entity owner
            User owner = userService.getUserWithAuthorities();
            entity.setOwner(owner);

            //CHeck for errors
            if ((errors != null) && errors.hasErrors()) {
                result.addErrors(entity, errors);
            }
        }

        return result;
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
        device.setRsaKey(deviceDetails.optString(MODEL_JSON_KEY_DEVICE_RSAKEY, ""));

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

        //Find adapter from repository and set it
        Adapter adapter = adapterRepository.findOne(componentDetails.optString(MODEL_JSON_KEY_COMPONENT_ADAPTER));
        component.setAdapter(adapter);

        return component;
    }
}
