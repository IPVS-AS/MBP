package org.citopt.connde.web.rest;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.Code;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.service.NetworkService;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.service.settings.SettingsService;
import org.citopt.connde.service.settings.model.BrokerLocation;
import org.citopt.connde.service.settings.model.Settings;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jan
 */
@org.springframework.stereotype.Component
public class ComponentDeploymentWrapper {

    @Autowired
    private SSHDeployer sshDeployer;

    public ResponseEntity isRunningComponent(Component component) {
        //Validity check
        if (component == null) {
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        //Determine component status
        Boolean result;
        try {
            result = sshDeployer.isComponentRunning(component);
        } catch (IOException e) {
            return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<Boolean>(result, HttpStatus.OK);
    }

    public ResponseEntity<ActionResponse> deployComponent(Component component, List<ParameterInstance> parameterInstances) {
        //Validity check
        if (component == null) {
            ActionResponse response = new ActionResponse(false, "The component does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Get adapter for parameter comparison
        Adapter adapter = component.getAdapter();

        //Iterate over all parameters
        for (Parameter parameter : adapter.getParameters()) {
            //Ignore parameter if not mandatory
            if (!parameter.isMandatory()) {
                continue;
            }

            //Iterate over all provided parameter instances and check if there is a matching one
            boolean matchFound = false;
            for (ParameterInstance parameterInstance : parameterInstances) {
                if (parameter.isInstanceValid(parameterInstance)) {
                    matchFound = true;
                    break;
                }
            }

            //Check if no valid instance was found for this parameter
            if (!matchFound) {
                ActionResponse response = new ActionResponse(false, "Invalid parameter configuration.");
                response.addFieldError("parameters", "Parameter \"" + parameter.getName() + "\" is invalid.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        }

        //Deploy component
        try {
            sshDeployer.deployComponent(component, parameterInstances);
        } catch (IOException e) {
            ActionResponse response = new ActionResponse(false, "An unknown error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Success
        ActionResponse response = new ActionResponse(true, "Success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    public ResponseEntity<ActionResponse> undeployComponent(Component component) {
        //Validity check
        if (component == null) {
            ActionResponse response = new ActionResponse(false, "The component does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Undeploy component
        try {
            sshDeployer.undeployComponent(component);
        } catch (IOException e) {
            ActionResponse response = new ActionResponse(false, "An unknown error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ActionResponse response = new ActionResponse(true, "Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    public ResponseEntity<Map<String, ComponentState>> getStatesAllComponents(List<Component> componentList) {
        //Create result map (component id -> component state)
        Map<String, ComponentState> resultMap = new HashMap<>();

        //Iterate over all components and determine their state
        for (Component component : componentList) {
            ComponentState state = sshDeployer.determineComponentState(component);
            resultMap.put(component.getId(), state);
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    public ResponseEntity<ComponentState> getComponentState(Component component) {
        //Validity check
        if (component == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Determine component state
        ComponentState componentState = sshDeployer.determineComponentState(component);

        return new ResponseEntity<>(componentState, HttpStatus.OK);
    }
}
