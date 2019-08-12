package org.citopt.connde.service.rules;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.rules.RuleExecutionResult;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.cep.engine.core.output.CEPOutput;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Component which takes care about executing the actions of given rules on demand.
 */
@Component
public class RuleExecutor {

    @Autowired
    private RuleRepository ruleRepository;

    /**
     * Tests the execution of a given rule action and returns whether the execution was successful.
     *
     * @param ruleAction The rule action to test
     * @return True, if the execution was successful; false otherwise
     */
    public boolean testRuleAction(RuleAction ruleAction) {
        //Sanity check
        if (ruleAction == null) {
            throw new IllegalArgumentException("Rule action must not be null.");
        }

        //Create testing rule as stub
        Rule testRule = new Rule();
        testRule.setId("0000000");
        testRule.setName("testing rule");
        testRule.setEnabled(true);
        testRule.setActions(Collections.singletonList(ruleAction));

        //Get executor for this rule action
        RuleActionExecutor executor = ruleAction.getType().getExecutor();

        //Execute rule action
        return executor.execute(ruleAction, testRule, null);
    }

    /**
     * Executes all actions of a given rule. In addition, output of a CEP engine that triggered the rule execution is passed.
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

        //Remembers if all actions have been executed successfully
        boolean success = true;

        //Iterate over all rule actions of the rule
        for (RuleAction ruleAction : rule.getActions()) {
            //Get responsible rule action executor
            RuleActionExecutor executor = ruleAction.getType().getExecutor();

            //Execute rule using the executor
            success &= executor.execute(ruleAction, rule, output);
        }

        //Update aftermath fields
        updateAftermath(rule, success);
    }

    /**
     * Updates the meta data of a rule, such as the number of executions and the date of the last execution.
     *
     * @param rule The rule to update
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

        //Write modified rule to repository
        ruleRepository.save(rule);
    }

    /**
     * Updates the aftermath fields of a rule pursuant to the result of a rule execution.
     *
     * @param rule    The rule to update
     * @param success True, if the rule execution was successful; false otherwise
     */
    private void updateAftermath(Rule rule, boolean success) {
        //Sanity check
        if (rule == null) {
            throw new IllegalArgumentException("Rule object most not be null.");
        }

        //Update execution result
        rule.setLastExecutionResult(success ? RuleExecutionResult.SUCCESS : RuleExecutionResult.FAILURE);

        //Write modified rule to repository
        ruleRepository.save(rule);
    }
}
