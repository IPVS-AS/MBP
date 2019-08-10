package org.citopt.connde.service.rules.execution;

import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Abstract base interface for rule action executors. Each executor takes care of executing
 * rule actions of a certain type.
 */
public interface RuleActionExecutor {

    /**
     * Validates a parameters map for the corresponding rule action type and updates
     * an errors object accordingly.
     *
     * @param errors     The errors object to update
     * @param parameters The parameters map (parameter name -> value) to validate
     */
    void validateParameters(Errors errors, Map<String, String> parameters);

    /**
     * Executes a given rule action of the corresponding rule action type. In addition, the output of a CEP engine that
     * triggered the execution of this rule action may be passed. The return value of this method indicates whether
     * the execution of the rule action was successful.
     *
     * @param action The rule action to execute
     * @param output The output of a CEP engine that triggered the execution of this rule action (may be null)
     * @return True, if the execution of the rule action was successful; false otherwise
     */
    boolean execute(RuleAction action, CEPOutput output);
}
