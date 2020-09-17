package org.citopt.connde.web.rest.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/**
 * Component that wraps the methods provided by the SSHDeployer in order to offer consistent deployment features
 * across various components. Each method of this component returns a response entity that can be directly
 * used in order to satisfy REST request that were issued by the user. Thus, this component ensures that all
 * deployment-related REST tasks offer the same behavior.
 */
@org.springframework.stereotype.Component
public class DeploymentWrapper {

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Checks if a component is currently running.
     *
     * @param component the {@link Component} to check.
     * @return {@code true} if and only the component is running; {@code false} otherwise.
     * @throws ResponseStatusException
     */
    public boolean isComponentRunning(Component component) throws ResponseStatusException {
        try {
        	// Determine component status
            return sshDeployer.isComponentRunning(component);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while checking the component status.");
        }
    }

    /**
	 * Starts a component using the given parameters.
	 *
	 * @param component the {@link Component} to start.
	 * @param parameterInstances the list of {@link ParameterInstance}s.
	 * @return the resulting {@link ActionResponse}.
	 */
    public ActionResponse startComponent(Component component, List<ParameterInstance> parameterInstances) {
        // Get adapter for parameter comparison
        Adapter adapter = component.getAdapter();

        for (Parameter parameter : adapter.getParameters()) {
            // Ignore parameter if not mandatory
            if (!parameter.isMandatory()) {
                continue;
            }

            // Iterate over all provided parameter instances and check if there is a matching one
            boolean matchFound = false;
            for (ParameterInstance parameterInstance : parameterInstances) {
                if (parameter.isInstanceValid(parameterInstance)) {
                    matchFound = true;
                    break;
                }
            }

            // Check if no valid instance was found for this parameter
            if (!matchFound) {
                ActionResponse response = new ActionResponse(false, "Invalid parameter configuration.");
                response.addFieldError("parameters", "Parameter \"" + parameter.getName() + "\" is invalid.");
                return response;
            }
        }

        try {
        	// Start component
            sshDeployer.startComponent(component, parameterInstances);
            return new ActionResponse(true, "Successfully started component!");
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while starting the component.");
        }
    }

    /**
     * Stops a component on its remote device.
     *
     * @param component the {@link Component} to stop.
     * @return {@code true} if and only if stopping the component succeeded; {@code false} otherwise.
     */
    public boolean stopComponent(Component component) {
        try {
        	// Undeploy component
            sshDeployer.stopComponent(component);
            return true;
        } catch (IOException e) {
        	return false;
        }
    }

    /**
     * Deploys a component onto its device.
     *
     * @param component the {@link Component} to deploy.
     * @return {@code true} if and only if the deployment succeeded; {@code false} otherwise.
     */
    public boolean deployComponent(Component component) {
        try {
        	// Deploy component
            sshDeployer.deployComponent(component);
            return true;
        } catch (IOException e) {
        	return false;
        }
    }

    /**
     * Undeploys a component onto its device.
     *
     * @param component the {@link Component} to undeploy.
     * @return {@code true} if and only if the undeployment succeeded; {@code false} otherwise.
     */
    public boolean undeployComponent(Component component) {
        try {
        	// Undeploy component
            sshDeployer.undeployComponent(component);
            return true;
        } catch (IOException e) {
        	return false;
        }
    }

	/**
	 * Retrieve the status for each given component.
	 *
	 * @param componentList the list of {@link Component}s.
	 * @return a map holding the {@link ComponentState} for each component identified by its id.
	 */
	public Map<String, ComponentState> getStatesAllComponents(List<Component> componentList) {
		// Create result map (component id -> component state)
		Map<String, ComponentState> resultMap = new HashMap<>();

		// Iterate over all components and determine their state
		for (Component component : componentList) {
			ComponentState state = sshDeployer.determineComponentState(component);
			resultMap.put(component.getId(), state);
		}

		return resultMap;
	}

	/**
	 * Retrieve the status for a given component.
	 *
	 * @param component the {@link Component}.
	 * @return the {@link ComponentState}.
	 */
	public ComponentState getComponentState(Component component) {
		// Determine component state
		return sshDeployer.determineComponentState(component);
	}
}
