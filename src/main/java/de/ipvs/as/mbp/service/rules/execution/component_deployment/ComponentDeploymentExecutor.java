package de.ipvs.as.mbp.service.rules.execution.component_deployment;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.rules.execution.RuleActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * Executor for component deployment actions including the deployment and undeployment of actuators and sensors.
 */
@Component
public class ComponentDeploymentExecutor implements RuleActionExecutor {

    //Parameter keys
    private static final String PARAM_KEY_COMPONENT = "component";
    private static final String PARAM_KEY_DEPLOY_ACTION = "deploy";

    //Regular expression describing permissible component strings
    private static final String REGEX_COMPONENT_STRING = "(?:actuator|sensor)/[A-z0-9]+";

    //Autowired
    private final ActuatorRepository actuatorRepository;

    //Autowired
    private final SensorRepository sensorRepository;

    //Autowired
    private final DeployerDispatcher deployerDispatcher;

    /**
     * Initializes the component deployment executor.
     *
     * @param actuatorRepository The actuator repository
     * @param sensorRepository   The sensor repository
     * @param deployerDispatcher The deployer dispatcher component
     */
    @Autowired
    public ComponentDeploymentExecutor(ActuatorRepository actuatorRepository, SensorRepository sensorRepository, DeployerDispatcher deployerDispatcher) {
        this.actuatorRepository = actuatorRepository;
        this.sensorRepository = sensorRepository;
        this.deployerDispatcher = deployerDispatcher;
    }

    /**
     * Validates a parameters map for the corresponding rule action type and will throw an exception
     * if a parameter is invalid.
     *
     * @param parameters The parameters map (parameter name -> value) to validate
     */
    @Override
    public void validateParameters(Map<String, String> parameters) {
        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");


        //Check component parameter
        if (parameters.containsKey(PARAM_KEY_COMPONENT)) {
            //Get component string
            String componentString = parameters.get(PARAM_KEY_COMPONENT);

            //Check if component string seems to be valid
            if ((componentString == null) || (componentString.length() < 3) || (!componentString.matches(REGEX_COMPONENT_STRING))) {
                exception.addInvalidField("parameters", "Invalid component provided.");
            } else {
                //Get component from component string
                de.ipvs.as.mbp.domain.component.Component component = getComponentFromString(componentString);

                //Check if component could be found
                if (component == null) {
                    exception.addInvalidField("parameters", "Component could not be found.");
                }
            }
        } else {
            //No actuator parameter available
            exception.addInvalidField("parameters", "A component needs to be selected.");
        }

        //Check deploy action parameter
        if (parameters.containsKey(PARAM_KEY_DEPLOY_ACTION)) {
            //Get deploy action
            String deployActionString = parameters.get(PARAM_KEY_DEPLOY_ACTION);

            //Try to get corresponding enum object
            try {
                DeploymentAction.valueOf(deployActionString);
            } catch (Exception e) {
                exception.addInvalidField("parameters", "Invalid deploy action provided.");
            }
        } else {
            //No subject parameter available
            exception.addInvalidField("parameters", "A deploy action needs to be selected.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
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
        //Find suitable deployer component
        IDeployer deployer = deployerDispatcher.getDeployer();

        //Get parameters
        Map<String, String> parameters = action.getParameters();
        String componentString = parameters.get(PARAM_KEY_COMPONENT);
        String deployActionString = parameters.get(PARAM_KEY_DEPLOY_ACTION);

        //Get component from component string
        de.ipvs.as.mbp.domain.component.Component component = getComponentFromString(componentString);

        //Return with failure if component not found
        if (component == null) {
            return false;
        }

        //Get deploy action from string
        DeploymentAction deploymentAction = DeploymentAction.valueOf(deployActionString);

        //Get current component state
        ComponentState componentState = deployer.retrieveComponentState(component);

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
                        deployer.deployComponent(component);
                    }

                    break;
                case START:
                    //Check component state
                    if (ComponentState.READY.equals(componentState)) {
                        //Component is ready, so deploy and start it
                        deployer.deployComponent(component);
                        deployer.startComponent(component, new ArrayList<>());
                    } else if (ComponentState.DEPLOYED.equals(componentState)) {
                        //Component is deployed, so just start it
                        deployer.startComponent(component, new ArrayList<>());
                    }

                    break;
                case STOP:
                    //Stop component if running
                    if (ComponentState.RUNNING.equals(componentState)) {
                        deployer.stopComponent(component);
                    }

                    break;
                case UNDEPLOY:
                    //Check component state
                    if (ComponentState.RUNNING.equals(componentState)) {
                        //Component is running, so stop and undeploy it
                        deployer.stopComponent(component);
                        deployer.undeployComponent(component);
                    } else if (ComponentState.DEPLOYED.equals(componentState)) {
                        //Component is deployed, so just undeploy it
                        deployer.undeployComponent(component);
                    }
                default:
                    return false;
            }

            //Check if component is now in target state
            ComponentState finalState = deployer.retrieveComponentState(component);
            return deploymentAction.getTargetState().equals(finalState);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the component of a component string that consists out of a component type and a component id.
     *
     * @param componentString The component string to parse
     * @return The retrieved component
     */
    private de.ipvs.as.mbp.domain.component.Component getComponentFromString(String componentString) {
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
