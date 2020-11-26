package org.citopt.connde.service.rules.execution.component_deployment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

/**
 * Executor for component deployment actions including the deployment and undeployment of actuators and sensors.
 */
@Component
public class ComponentDeploymentExecutor implements RuleActionExecutor {

    //Parameter keys
    private static final String PARAM_KEY_COMPONENT = "component";
    private static final String PARAM_KEY_DEPLOY_ACTION = "deploy";

    //Regular expression describing permissible component strings
    private static final String REGEX_COMPONENT_STRING = "(?:actuator|sensor)\\/[A-z0-9]+";

    //Autowired
    private ActuatorRepository actuatorRepository;

    //Autowired
    private SensorRepository sensorRepository;

    //Autowired
    private SSHDeployer sshDeployer;

    /**
     * Initializes the component deployment executor.
     *
     * @param actuatorRepository The actuator repository
     * @param sensorRepository   The sensor repository
     * @param sshDeployer        The SSH deployer service
     */
    @Autowired
    public ComponentDeploymentExecutor(ActuatorRepository actuatorRepository, SensorRepository sensorRepository, SSHDeployer sshDeployer) {
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;
        this.sshDeployer = sshDeployer;
    }

    /**
     * Validates a parameters map for the corresponding rule action type and updates
     * an errors object accordingly.
     *
     * @param errors     The errors object to update
     * @param parameters The parameters map (parameter name -> value) to validate
     */
    @Override
    public void validateParameters(Errors errors, Map<String, String> parameters) {
        //Check component parameter
        if (parameters.containsKey(PARAM_KEY_COMPONENT)) {
            //Get component string
            String componentString = parameters.get(PARAM_KEY_COMPONENT);

            //Check if component string seems to be valid
            if ((componentString == null) || (componentString.length() < 3) || (!componentString.matches(REGEX_COMPONENT_STRING))) {
                errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                        "Invalid component provided.");
            } else {
                //Get component from component string
                org.citopt.connde.domain.component.Component component = getComponentFromString(componentString);

                //Check if component could be found
                if (component == null) {
                    errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                            "Component could not be found.");
                }
            }
        } else {
            //No actuator parameter available
            errors.rejectValue("parameters", "ruleAction.parameters.missing",
                    "A component needs to be selected.");
        }

        //Check deploy action parameter
        if (parameters.containsKey(PARAM_KEY_DEPLOY_ACTION)) {
            //Get deploy action
            String deployActionString = parameters.get(PARAM_KEY_DEPLOY_ACTION);

            //Try to get corresponding enum object
            try {
                DeploymentAction.valueOf(deployActionString);
            } catch (Exception e) {
                errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                        "Invalid deploy action provided.");
            }
        } else {
            //No subject parameter available
            errors.rejectValue("parameters", "ruleAction.parameters.missing",
                    "A deploy action needs to be selected.");
        }
    }

    /**
     * Executes an given action of a given rule that is of the corresponding rule action type. In addition, the output
     * of a CEP engine that triggered the execution may be passed. The return value of this method indicates whether
     * the execution of the rule action was successful.
     *
     * @param action The rule action to execute
     * @param rule   The rule that holds the action that is supposed to be executed
     * @param output The output of a CEP engine that triggered the execution of this rule action (may be null)
     * @return True, if the execution of the rule action was successful; false otherwise
     */
    @Override
    public boolean execute(RuleAction action, Rule rule, CEPOutput output) {
        //Get parameters
        Map<String, String> parameters = action.getParameters();
        String componentString = parameters.get(PARAM_KEY_COMPONENT);
        String deployActionString = parameters.get(PARAM_KEY_DEPLOY_ACTION);

        //Get component from component string
        org.citopt.connde.domain.component.Component component = getComponentFromString(componentString);

        //Return with failure if component not found
        if (component == null) {
            return false;
        }

        //Get deploy action from string
        DeploymentAction deploymentAction = DeploymentAction.valueOf(deployActionString);

        //Get current component state
        ComponentState componentState = sshDeployer.determineComponentState(component);

        //Return with failure if component is not available
        if (ComponentState.UNKNOWN.equals(componentState) || ComponentState.NOT_READY.equals(componentState)) {
            return false;
        }

        //Execute action
        try {
            //Case differentiation for deployment actions
            switch (deploymentAction) {
                case DEPLOY:
                    //Deploy component if ready
                    if (ComponentState.READY.equals(componentState)) {
                        sshDeployer.deployComponent(component);
                    }

                    break;
                case START:
                    //Check component state
                    if (ComponentState.READY.equals(componentState)) {
                        //Component is ready, so deploy and start it
                        sshDeployer.deployComponent(component);
                        sshDeployer.startComponent(component, new ArrayList<>());
                    } else if (ComponentState.DEPLOYED.equals(componentState)) {
                        //Component is deployed, so just start it
                        sshDeployer.startComponent(component, new ArrayList<>());
                    }

                    break;
                case STOP:
                    //Stop component if running
                    if (ComponentState.RUNNING.equals(componentState)) {
                        sshDeployer.stopComponent(component);
                    }

                    break;
                case UNDEPLOY:
                    //Check component state
                    if (ComponentState.RUNNING.equals(componentState)) {
                        //Component is running, so stop and undeploy it
                        sshDeployer.stopComponent(component);
                        sshDeployer.undeployComponent(component);
                    } else if (ComponentState.DEPLOYED.equals(componentState)) {
                        //Component is deployed, so just undeploy it
                        sshDeployer.undeployComponent(component);
                    }
                default:
                    return false;
            }

            //Check if component is now in target state
            ComponentState finalState = sshDeployer.determineComponentState(component);
            return deploymentAction.getTargetState().equals(finalState);

        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Returns the component of a component string that consists out of a component type and a component id.
     *
     * @param componentString The component string to parse
     * @return The retrieved component
     */
    private org.citopt.connde.domain.component.Component getComponentFromString(String componentString) {
        if ((componentString == null) || componentString.isEmpty()) {
            throw new IllegalArgumentException("Component string must not be null or empty.");
        }

        //Split component string and get component type and id
        String[] splits = componentString.split("/");
        String componentType = splits[0];
        String componentId = splits[1];

        //Get component by doing a case differentiation for component types
        if (componentType.equals(new Actuator().getComponentTypeName())) {
            return actuatorRepository.findById(componentId).get();
        } else if (componentType.equals(new Sensor().getComponentTypeName())) {
            return sensorRepository.findById(componentId).get();
        }

        //No matching component type found
        return null;
    }
}
