package org.citopt.connde.service.rules;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RuleEngine {

    private RuleRepository ruleRepository;

    private CEPTriggerService triggerService;

    private RuleExecutor ruleExecutor;

    private Map<RuleTrigger, Set<Rule>> triggerMap;

    @Autowired
    private RuleEngine(RuleRepository ruleRepository, CEPTriggerService triggerService, RuleExecutor ruleExecutor) {
        this.ruleRepository = ruleRepository;
        this.triggerService = triggerService;
        this.ruleExecutor = ruleExecutor;

        //Initialize trigger map
        triggerMap = new HashMap<>();

        //Load available rules
        loadRulesOnStartup();
    }

    public void enableRule(Rule rule) {
        //Sanity check
        if (rule == null) {
            throw new IllegalArgumentException("Rule must not be null.");
        }

        //Get rule trigger
        RuleTrigger trigger = rule.getTrigger();

        //Add the rule to the trigger map, if trigger is already registered
        if (triggerMap.containsKey(trigger)) {
            Set<Rule> rulesOfTrigger = triggerMap.get(trigger);
            rulesOfTrigger.add(rule);
        } else {
            //Register trigger at the trigger service
            triggerService.registerTrigger(trigger, (ruleTrigger, output) -> {
                //Induce the executions of rules that use this trigger on callback
                induceRuleExecution(ruleTrigger);
            });

            Set<Rule> rulesOfTrigger = new HashSet<>();
            rulesOfTrigger.add(rule);
            triggerMap.put(trigger, rulesOfTrigger);
        }

        rule.setEnabled(true);
        ruleRepository.save(rule);
    }

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

    private void induceRuleExecution(RuleTrigger ruleTrigger) {
        //Sanity check
        if (ruleTrigger == null) {
            throw new IllegalArgumentException("Rule object most not be null.");
        }

        //Get all rules from the map that use the given trigger
        Set<Rule> ruleSet = triggerMap.get(ruleTrigger);

        //Iterate over all rules and execute them
        for (Rule rule : ruleSet) {
            ruleExecutor.executeRule(rule);
        }
    }

    private void loadRulesOnStartup() {
        List<Rule> rules = ruleRepository.findAll();

        for (Rule rule : rules) {
            if (rule.isEnabled()) {
                enableRule(rule);
            }
        }
    }
}
