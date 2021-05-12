package de.ipvs.as.mbp.web.rest.helper;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.error.DeploymentException;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Component that wraps the methods provided by the SSHDeployer in order to offer consistent deployment features
 * across various components. Each method of this component returns a response entity that can be directly
 * used in order to satisfy REST request that were issued by the user. Thus, this component ensures that all
 * deployment-related REST tasks offer the same behavior.
 */
@org.springframework.stereotype.Component
public class DeploymentWrapper {

    @Autowired
    private DeployerDispatcher deployerDispatcher;

    /**
     * Checks if a component is currently running.
     *
     * @param component the {@link Component} to check.
     * @return {@code true} if and only the component is running; {@code false} otherwise.
     */
    public boolean isComponentRunning(Component component) {
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Determine component status
        return deployer.isComponentRunning(component);
    }

    /**
     * Starts a component using the given parameters.
     *
     * @param component          the {@link Component} to start.
     * @param parameterInstances the list of {@link ParameterInstance}s.
     */
    public void     startComponent(Component component, List<ParameterInstance> parameterInstances) {
        // Get adapter for parameter comparison
        Operator operator = component.getOperator();

        for (Parameter parameter : operator.getParameters()) {
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
                throw new DeploymentException("Invalid parameter configuration.").addInvalidParameter(parameter.getName(), "Parameter " + parameter.getName() + " is invalid or missing.");
            }
        }

        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Start component
        deployer.startComponent(component, parameterInstances);
    }

    /**
     * Stops a component on its remote device.
     *
     * @param component the {@link Component} to stop.
     */
    public void stopComponent(Component component) {
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Undeploy component
        deployer.stopComponent(component);
    }

    /**
     * Deploys a component onto its device.
     *
     * @param component the {@link Component} to deploy.
     */
    public void deployComponent(Component component) {
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Deploy component
        deployer.deployComponent(component);
    }

    /**
     * Undeploys a component onto its device.
     *
     * @param component the {@link Component} to undeploy.
     */
    public void undeployComponent(Component component) {
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Undeploy component
        deployer.undeployComponent(component);
    }

    /**
     * Retrieve the status for each given component.
     *
     * @param componentList the list of {@link Component}s.
     * @return a map holding the {@link ComponentState} for each component identified by its id.
     */
    public Map<String, ComponentState> getStatesAllComponents(List<Component> componentList) {
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Create result map (component id -> component state)
        Map<String, ComponentState> resultMap = new HashMap<>();

        // Iterate over all components and determine their state
        for (Component component : componentList) {
            ComponentState state = deployer.retrieveComponentState(component);
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
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        // Determine component state
        return deployer.retrieveComponentState(component);
    }
}
