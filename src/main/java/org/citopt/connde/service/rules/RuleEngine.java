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

    private Map<RuleTrigger, Set<Rule>> triggerMap;

    @Autowired
    private RuleEngine(RuleRepository ruleRepository, CEPTriggerService triggerService) {
        this.ruleRepository = ruleRepository;
        this.triggerService = triggerService;

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
            triggerService.registerTrigger(trigger, (ruleTrigger, output) -> {
                System.out.println("asdfasdfasdf");
            });

            Set<Rule> rulesOfTrigger = new HashSet<>();
            rulesOfTrigger.add(rule);
            triggerMap.put(trigger, rulesOfTrigger);
        }

        rule.setEnabled(true);
        ruleRepository.save(rule);
    }

    public void disableRule(Rule rule) {

    }

    private void loadRulesOnStartup() {
        List<Rule> rules = ruleRepository.findAll();

        for (Rule rule : rules) {
            //if (rule.isEnabled()) {
            enableRule(rule);
            //}
        }
    }
}
