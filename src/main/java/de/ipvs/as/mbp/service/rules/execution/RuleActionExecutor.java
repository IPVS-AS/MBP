package de.ipvs.as.mbp.service.rules.execution;

import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import org.springframework.validation.Errors;

import java.util.Map;

/**
 * Abstract base interface for rule action executors. Each executor takes care of executing
 * rule actions of a certain type.
 */
public interface RuleActionExecutor {

    /**
     * Validates a parameters map for the corresponding rule action type and will throw an exception
     * if a parameter is invalid.
     *
     * @param parameters The parameters map (parameter name -> value) to validate
     */
    void validateParameters(Map<String, String> parameters);

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
    boolean execute(RuleAction action, Rule rule, CEPOutput output);
}
