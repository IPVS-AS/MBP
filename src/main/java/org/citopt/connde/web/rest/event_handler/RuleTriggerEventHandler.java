package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.rules.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Event handler for operations that are performed on rule triggers.
 */
@Component
@RepositoryEventHandler
public class RuleTriggerEventHandler {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleEngine ruleEngine;

    /**
     * Called, when a rule trigger is supposed to be deleted. This method then takes care of deleting
     * the rules as well that make use of this rule trigger.
     *
     * @param ruleTrigger The rule trigger that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeRuleTriggerDelete(RuleTrigger ruleTrigger) {
        //Get trigger id
        String id = ruleTrigger.getId();

        //Get rules that are affected by this trigger
        List<Rule> affectedRules = ruleRepository.findAllByTriggerId(id);

        //Iterate over all affected rules, disable and delete them
        for(Rule affectedRule : affectedRules){
            ruleEngine.disableRule(affectedRule);
            ruleRepository.delete(affectedRule);
        }
    }
}