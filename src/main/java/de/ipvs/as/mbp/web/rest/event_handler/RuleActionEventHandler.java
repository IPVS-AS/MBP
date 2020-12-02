package de.ipvs.as.mbp.web.rest.event_handler;

import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.repository.RuleRepository;
import de.ipvs.as.mbp.service.rules.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Event handler for operations that are performed on rule actions.
 */
@Component
@RepositoryEventHandler
public class RuleActionEventHandler {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleEngine ruleEngine;

    /**
     * Called, when a rule action is supposed to be deleted. This method then takes care of deleting
     * the rules as well that make use of this rule action.
     *
     * @param ruleAction The rule action that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeRuleActionDelete(RuleAction ruleAction) {
        //Get rules that are affected by this action
        for (Rule rule : ruleRepository.findAll()) {
            //Get rule actions of the current rule
            List<RuleAction> ruleActions = rule.getActions();

            //Check if the affected action is among these rule actions
            if(ruleActions.contains(ruleAction)){
                //Disable rule
                ruleEngine.disableRule(rule);

                //Delete rule
                ruleRepository.delete(rule);
            }
        }
    }
}