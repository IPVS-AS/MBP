package org.citopt.connde.service.rules.execution.executors;

import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.citopt.connde.service.mqtt.MQTTService;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Executor for device notification actions.
 */
@Component
public class DeviceNotificationExecutor implements RuleActionExecutor {

    //Parameter keys
    private static final String PARAM_KEY_DEVICE = "device";
    private static final String PARAM_KEY_SUBJECT = "subject";


    //Autowired
    private DeviceRepository deviceRepository;

    //Autowired
    private MQTTService mqttService;

    /**
     * Initializes the device notification executor component.
     *
     * @param deviceRepository The device repository (autowired)
     * @param mqttService      The MQTT service (autowired)
     */
    @Autowired
    public DeviceNotificationExecutor(DeviceRepository deviceRepository, MQTTService mqttService) {
        this.deviceRepository = deviceRepository;
        this.mqttService = mqttService;
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
        //Get parameters from map

        //Make sure all required parameters are provided
        if (parameters.containsKey(PARAM_KEY_DEVICE)) {
            //Get device id
            String deviceId = parameters.get(PARAM_KEY_DEVICE);

            //Check if device id is valid
            if ((deviceId == null) || deviceId.isEmpty() || (!deviceRepository.exists(deviceId))) {
                errors.rejectValue("parameters", "ruleAction.parameters.invalid",
                        "Invalid device selected.");
            }
        } else {
            //No device parameter available
            errors.rejectValue("parameters", "ruleAction.parameters.missing",
                    "A device needs to be selected.");
        }

        if (parameters.containsKey(PARAM_KEY_SUBJECT)) {
            //Get subject
            String subject = parameters.get(PARAM_KEY_SUBJECT);

            //Validate subject
            if ((subject == null) || subject.isEmpty()) {
                errors.rejectValue("parameters", "ruleAction.parameters.empty",
                        "The subject must not be empty..");
            }
        } else {
            //No subject parameter available
            errors.rejectValue("parameters", "ruleAction.parameters.missing",
                    "A subject needs to be provided.");
        }
    }


    /**
     * Executes a given rule action of the corresponding rule action type. In addition, the output of a CEP engine that
     * triggered the execution of this rule action is passed. The return value of this method indicates whether
     * the execution of the rule action was successful.
     *
     * @param action The rule action to execute
     * @param output The output of a CEP engine that triggered the execution of this rule action
     * @return True, if the execution of the rule action was successful; false otherwise
     */
    @Override
    public boolean execute(RuleAction action, CEPOutput output) {
        System.out.println("asdfasdf lalala");
        return false;
    }
}
