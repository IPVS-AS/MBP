package de.ipvs.as.mbp.service.rules;

import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.Testing;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.repository.TestRepository;
import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQueryValidation;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * The rule engine component manages all rules and provides means for enabling and disabling them. In addition,
 * it takes care about inducing the execution of a rule in case it was triggering.
 */
@Component
public class RuleEngine {

    private final RuleRepository ruleRepository;

    private final CEPTriggerService triggerService;

    private final RuleExecutor ruleExecutor;

    private final Map<RuleTrigger, Set<Rule>> triggerMap;

    private final TestRepository testRepo;

    /**
     * Initializes the rule engine component and activates all already enabled rules.
     *
     * @param ruleRepository The repository in which the rules are stored (autowired)
     * @param triggerService The CEP trigger service to use (autowired)
     * @param ruleExecutor   The rule executor to use (autowired)
     */
    @Autowired
    private RuleEngine(RuleRepository ruleRepository, CEPTriggerService triggerService, RuleExecutor ruleExecutor, TestRepository testRepo) {
        this.ruleRepository = ruleRepository;
        this.triggerService = triggerService;
        this.ruleExecutor = ruleExecutor;
        this.testRepo = testRepo;

        //Initialize trigger map
        triggerMap = new HashMap<>();

        //Load available rules
        loadRulesOnStartup();
    }

    /**
     * Enables a certain rule so that it will be possible to trigger and execute it. The return value indicates
     * whether enabling the rule was successful or not.
     *
     * @param rule The rule to enable
     * @return True, if the rule was enabled successfully; false otherwise
     */
    public boolean enableRule(Rule rule) {
        //Sanity check
        if (rule == null) {
            throw new IllegalArgumentException("Rule must not be null.");
        }

        //Get rule trigger
        RuleTrigger trigger = rule.getTrigger();

        //Check if trigger is still valid (all components exist)
        CEPQueryValidation validationResult = triggerService.isValidTriggerQuery(trigger);

        if (!validationResult.isValid()) {
            //Trigger is not valid, mark it as disabled
            rule.setEnabled(false);
            ruleRepository.save(rule);

            return false;
        }

        //Add the rule to the trigger map, if trigger is already registered
        if (triggerMap.containsKey(trigger)) {
            Set<Rule> rulesOfTrigger = triggerMap.get(trigger);
            rulesOfTrigger.add(rule);
        } else {
            //Register trigger at the trigger service
            triggerService.registerTrigger(trigger, (ruleTrigger, output) -> {
                //Induce the executions of rules that use this trigger on callback
                induceRuleExecution(ruleTrigger, output);

            });

            Set<Rule> rulesOfTrigger = new HashSet<>();
            rulesOfTrigger.add(rule);
            triggerMap.put(trigger, rulesOfTrigger);
        }

        //Enable rule and save it
        rule.setEnabled(true);
        ruleRepository.save(rule);

        //Everything successful
        return true;
    }

    /**
     * Disables a certain rule so that it will not be triggered and executed anymore.
     *
     * @param rule The rule to disable
     */
    public void disableRule(Rule rule) {
        //Sanity check
        if (rule == null) {
            throw new IllegalArgumentException("Rule must not be null.");
        }

        //Get rule trigger
        RuleTrigger trigger = rule.getTrigger();

        if (!triggerMap.containsKey(trigger)) {
            return;
        }

        //Get set of rules for this trigger
        Set<Rule> rules = triggerMap.get(trigger);

        //Remove rule from set
        rules.remove(rule);

        //Check if rule set is now empty
        if (rules.isEmpty()) {
            //Unregister trigger from trigger service
            triggerService.unregisterTrigger(trigger);

            //Remove entry from trigger map
            triggerMap.remove(trigger);
        }

        rule.setEnabled(false);
        ruleRepository.save(rule);
    }

    /**
     * Induces the execution of rules that have a certain rule trigger. In addition,
     * output of a CEP engine that triggered the trigger is passed.
     *
     * @param ruleTrigger The rule trigger
     * @param output      The CEP output to pass
     */
    public void induceRuleExecution(RuleTrigger ruleTrigger, CEPOutput output) {
        //Sanity check
        if (ruleTrigger == null) {
            throw new IllegalArgumentException("Rule object most not be null.");
        }

        //Get all rules from the map that use the given trigger
        Set<Rule> ruleSet = triggerMap.get(ruleTrigger);
        Set<String> ruleNames = new HashSet<>();

        //Iterate over all rules and execute them
        for (Rule rule : ruleSet) {

            ruleNames.add(rule.getName());
            Testing testing = new Testing();
            testing.setTrigger(ruleTrigger);
            testing.setOutput(output);
            testing.setRule(ruleNames);


            testRepo.insert(testing);
            ruleExecutor.executeRule(rule, output);
        }


    }

    /**
     * Ensures that enabled rules will be active and working after the startup of the application.
     */
    private void loadRulesOnStartup() {
        //Get all rules
        List<Rule> rules = ruleRepository.findAll();

        //Iterate over all rules
        for (Rule rule : rules) {
            //Enable rule if it is enabled
            if (rule.isEnabled()) {
                enableRule(rule);
            }
        }
    }
}
