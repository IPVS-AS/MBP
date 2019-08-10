package org.citopt.connde.service.rules.execution.executors;

import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Executor for IFTTT webhook (https://ifttt.com/maker_webhooks) actions.
 */
@Component
public class IFTTTWebhookExecutor implements RuleActionExecutor {

    /**
     * Validates a parameters map for the corresponding rule action type and updates
     * an errors object accordingly.
     *
     * @param errors     The errors object to update
     * @param parameters The parameters map (parameter name -> value) to validate
     */
    @Override
    public void validateParameters(Errors errors, Map<String, String> parameters) {

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
        return false;
    }
}
