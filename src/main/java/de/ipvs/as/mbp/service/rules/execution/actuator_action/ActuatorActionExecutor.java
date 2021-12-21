package de.ipvs.as.mbp.service.rules.execution.actuator_action;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import de.ipvs.as.mbp.service.mqtt.MQTTService;
import de.ipvs.as.mbp.service.rules.execution.RuleActionExecutor;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Executor for actuator actions.
 */
@Component
public class ActuatorActionExecutor implements RuleActionExecutor {

    /*
    Frame of the MQTT topic to use for notifying actuators
    Format: action/{actuator id}/{action name}
     */
    private static final String MQTT_TOPIC = "action/%s/%s";

    //Parameter keys
    private static final String PARAM_KEY_ACTUATOR = "actuator";
    private static final String PARAM_KEY_ACTION_NAME = "action";
    private static final String PARAM_KEY_DATA = "data";

    //Regular expression describing permissible action names
    private static final String REGEX_ACTION_NAME = "[A-z0-9_\\- ]+";

    //Autowired
    private final ActuatorRepository actuatorRepository;

    //Autowired
    private final MQTTService mqttService;

    /**
     * Initializes the actuator action executor component.
     *
     * @param actuatorRepository The actuator repository (autowired)
     * @param mqttService        The MQTT service (autowired)
     */
    @Autowired
    public ActuatorActionExecutor(ActuatorRepository actuatorRepository, MQTTService mqttService) {
        this.actuatorRepository = actuatorRepository;
        this.mqttService = mqttService;
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

        //Check actuator parameter
        if (parameters.containsKey(PARAM_KEY_ACTUATOR)) {
            //Get actuator id
            String actuatorId = parameters.get(PARAM_KEY_ACTUATOR);

            //Check if actuator id is valid
            if ((actuatorId == null) || actuatorId.isEmpty() || (!actuatorRepository.existsById(actuatorId))) {
                exception.addInvalidField("parameters", "Invalid actuator selected.");
            }
        } else {
            //No actuator parameter available
            exception.addInvalidField("parameters", "An actuator needs to be selected.");
        }

        //Check action name parameter
        if (parameters.containsKey(PARAM_KEY_ACTION_NAME)) {
            //Get action name
            String actionName = parameters.get(PARAM_KEY_ACTION_NAME);

            //Validate action name
            if ((actionName == null) || actionName.isEmpty()) {
                exception.addInvalidField("parameters", "The action name must not be empty.");
            } else if (!actionName.matches(REGEX_ACTION_NAME)) {
                exception.addInvalidField("parameters", "The action name contains invalid characters.");
            }
        } else {
            //No subject parameter available
            exception.addInvalidField("parameters", "A subject needs to be provided.");
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
     * @param action    The rule action to execute
     * @param rule      The rule that holds the action that is supposed to be executed
     * @param cepOutput The output of a CEP engine that triggered the execution of this rule action (may be null)
     * @return True, if the execution of the rule action was successful; false otherwise
     */
    @Override
    public boolean execute(RuleAction action, Rule rule, CEPOutput cepOutput) {
        //Get action parameters
        Map<String, String> parameters = action.getParameters();
        String actuatorId = parameters.get(PARAM_KEY_ACTUATOR);
        String actionName = parameters.get(PARAM_KEY_ACTION_NAME);
        String data = parameters.get(PARAM_KEY_DATA);

        //Get actuator from repository
        Actuator actuator = actuatorRepository.findById(actuatorId).get();

        //Sanity check
        if (actuator == null) {
            return false;
        }

        //Sanitize data
        if (data == null) {
            data = "";
        }

        //Sanitize CEP output
        if (cepOutput == null) {
            cepOutput = new CEPOutput();
        }

        //Build JSON object that carries all information
        JSONObject messageObject = new JSONObject();
        try {
            messageObject.put("rule_id", rule.getId());
            messageObject.put("rule_name", rule.getName());
            messageObject.put("rule_action_id", action.getId());
            messageObject.put("rule_action_name", action.getName());
            messageObject.put("actuator_id", actuatorId);
            messageObject.put("action", actionName);
            messageObject.put("data", data);
            messageObject.put("cep_output", cepOutput.getOutputMap());
        } catch (JSONException e) {
            return false;
        }

        //Generate MQTT topic for this actuator and action name
        String topic = generateMQTTTopic(actuator, actionName);

        //Get string from JSON object
        String message = messageObject.toString();

        //Publish JSON object as sting
        try {
            mqttService.publish(topic, message);
        } catch (MqttException e) {
            return false;
        }

        return true;
    }

    /**
     * Generates a new MQTT topic for notifying a certain actuator to execute an action with a certain name.
     * The topic generation is deterministic for a fixed actuator and action name.
     *
     * @param actuator   The actuator to generate the topic for
     * @param actionName The name of the action the execute
     * @return The generated MQTT topic
     */
    private String generateMQTTTopic(Actuator actuator, String actionName) {
        //Sanity check
        if (actuator == null) {
            throw new IllegalArgumentException("Actuator must not be null.");
        } else if ((actionName == null) || actionName.isEmpty()) {
            throw new IllegalArgumentException("Action name must not be null or empty.");
        }

        //Format topic and return it
        return String.format(MQTT_TOPIC, actuator.getId(), actionName);
    }
}
