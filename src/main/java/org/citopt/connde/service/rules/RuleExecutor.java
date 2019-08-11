package org.citopt.connde.service.rules;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component which takes care about executing the actions of given rules on demand.
 */
@Component
public class RuleExecutor {

    @Autowired
    private RuleRepository ruleRepository;

    /**
     * Executes the action of a given rule. In addition, output of a CEP engine that triggered the rule execution is passed.
     *
     * @param rule   The rule to execute
     * @param output The output to pass
     */
    public void executeRule(Rule rule, CEPOutput output) {
        //Sanity check
        if (rule == null) {
            throw new IllegalArgumentException("Rule object most not be null.");
        }

        //Update meta data
        updateRuleMetaData(rule);

        //Get rule action
        RuleAction ruleAction = rule.getAction();

        //Get responsible rule action executor
        RuleActionExecutor executor = ruleAction.getType().getExecutor();

        //Execute rule using the executor
        executor.execute(rule, output);
    }

    /**
     * Updates the meta data of a rule, such as the number of executions and the date of the last execution.
     *
     * @param rule The rule that is supposed to be updated
     */
    private void updateRuleMetaData(Rule rule) {
        //Sanity check
        if (rule == null) {
            throw new IllegalArgumentException("Rule object most not be null.");
        }

        //Increase number of executions
        rule.increaseExecutions();

        //Update date of last execution
        rule.setLastExecutionToNow();

        //Write modified rule into repository
        ruleRepository.save(rule);
    }
}
