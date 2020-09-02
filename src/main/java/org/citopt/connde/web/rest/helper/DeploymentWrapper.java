package org.citopt.connde.web.rest.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Component that wraps the methods provided by the SSHDeployer in order to offer consistent deployment features
 * across various components. Each method of this component returns a response entity that can be directly
 * used in order to satisfy REST request that were issued by the user. Thus, this component ensures that all
 * deployment-related REST tasks offer the same behaviour.
 */
@org.springframework.stereotype.Component
public class DeploymentWrapper {

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Checks if a component is currently running. If this is the case, true is returned; otherwise false.
     *
     * @param component The component to check
     * @return A ResponseEntity object that contains the result
     */
    public ResponseEntity<Boolean> isComponentRunning(Component component) {
        //Validity check
        if (component == null) {
            return new ResponseEntity<Boolean>(HttpStatus.NOT_FOUND);
        }

        //Security check
        if (!userEntityService.isUserPermitted(component, "deploy")) {
            return new ResponseEntity<>(Boolean.FALSE, HttpStatus.UNAUTHORIZED);
        }

        //Determine component status
        Boolean result;
        try {
            result = sshDeployer.isComponentRunning(component);
        } catch (IOException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    /**
     * Starts a component by using a list of parameter instances that contain a corresponding value.
     *
     * @param component          The component to start
     * @param parameterInstances A list of parameter instances to use for the start request
     * @return A ResponseEntity object that contains an ActionResponse which describes the result of the start request
     */
    public ResponseEntity<ActionResponse> startComponent(Component component, List<ParameterInstance> parameterInstances) {
        //Validity check
        if (component == null) {
            ActionResponse response = new ActionResponse(false, "The component does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Security check
        if (!userEntityService.isUserPermitted(component, "deploy")) {
            ActionResponse response = new ActionResponse(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
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

        //Start component
        try {
            sshDeployer.startComponent(component, parameterInstances);
        } catch (IOException e) {
            ActionResponse response = new ActionResponse(false, "An unknown error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Success
        ActionResponse response = new ActionResponse(true, "Success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Stops a component on its remote device.
     *
     * @param component The component to stop
     * @return A ResponseEntity object that contains an ActionResponse which describes the result of the stop request
     */
    public ResponseEntity<ActionResponse> stopComponent(Component component) {
        //Validity check
        if (component == null) {
            ActionResponse response = new ActionResponse(false, "The component does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Security check
        if (!userEntityService.isUserPermitted(component, "deploy")) {
            ActionResponse response = new ActionResponse(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //Undeploy component
        try {
            sshDeployer.stopComponent(component);
        } catch (IOException e) {
            ActionResponse response = new ActionResponse(false, "An unknown error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ActionResponse response = new ActionResponse(true, "Success");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Deploys a component onto its device.
     *
     * @param component The component to deploy
     * @return A ResponseEntity object that contains an ActionResponse which describes the result of the deployment
     */
    public ResponseEntity<ActionResponse> deployComponent(Component component) {
        //Validity check
        if (component == null) {
            ActionResponse response = new ActionResponse(false, "The component does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Security check
        if (!userEntityService.isUserPermitted(component, "deploy")) {
            ActionResponse response = new ActionResponse(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        //Deploy component
        try {
            sshDeployer.deployComponent(component);
        } catch (IOException e) {
            ActionResponse response = new ActionResponse(false, "An unknown error occurred");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Success
        ActionResponse response = new ActionResponse(true, "Success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Deploys a component onto its remote device.
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

        //Security check
        if (!userEntityService.isUserPermitted(component, "deploy")) {
            ActionResponse response = new ActionResponse(false);
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
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
    public ResponseEntity<EntityModel<ComponentState>> getComponentState(Component component) {
        //Validity check
        if (component == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Security check
        if (!userEntityService.isUserPermitted(component, "deploy")) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        //Determine component state
        ComponentState componentState = sshDeployer.determineComponentState(component);

        //Wrap component state into resource
        EntityModel<ComponentState> stateResource = new EntityModel<>(componentState);

        return new ResponseEntity<>(stateResource, HttpStatus.OK);
    }
}
