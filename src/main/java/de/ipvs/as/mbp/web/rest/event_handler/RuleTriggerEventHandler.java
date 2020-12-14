package de.ipvs.as.mbp.web.rest.event_handler;

import java.util.List;

import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

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