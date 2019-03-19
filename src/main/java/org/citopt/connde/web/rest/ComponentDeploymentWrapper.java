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
 * Component that wraps the methods provided by the SSHDeployer in order to offer consistent deployment features
 * across various components. Each method of this component returns a response entity that can be directly
 * used in order to satisfy REST request that were issued by the user. Thus, this component ensures that all
 * deployment-related REST tasks offer the same behaviour.
 *
 * @author Jan
 */
@org.springframework.stereotype.Component
public class ComponentDeploymentWrapper {

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Checks if a component is currently running. If this is the case, true is returned; otherwise false.
     *
     * @param component The component to check
     * @return A ResponseEntity object that contains the result
     */
    public ResponseEntity<Boolean> isRunningComponent(Component component) {
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

    /**
     * Deploys a component by using a list of parameter instances that contain a corresponding value.
     *
     * @param component          The component to deploy
     * @param parameterInstances A list of parameter instances to use for the deployment
     * @return A ResponseEntity object that contains an ActionResponse which describes the result of the deployment
     */
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

    /**
     * Deploys a component by using a list of parameter instances that contain a corresponding value.
     *
     * @param component The component to undeploy
     * @return A ResponseEntity object that contains an ActionResponse which describes the result of the undeployment
     */
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

    /**
     * Retrieves a map that holds the current state for each component that is part of a given list.
     *
     * @param componentList A list of components for which the single states should be determined
     * @return A ResponseEntity object that contains a map (component id -> component state) which holds
     * the state for each component
     */
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

    /**
     * Retrieves the current component state for a given component.
     *
     * @param component The component for which the state is supposed to be determined
     * @return A ResponseEntity object that holds the component state of the component
     */
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
