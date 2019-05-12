package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.repository.RuleRepository;
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

    /**
     * Called, when a rule action is supposed to be deleted. This method then takes care of deleting
     * the rules as well that make use of this rule action.
     *
     * @param ruleAction The rule action that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeRuleActionDelete(RuleAction ruleAction) {
        //Get action id
        String id = ruleAction.getId();

        //Get rules that are affected by this action
        List<Rule> affectedRules = ruleRepository.findAllByActionId(id);

        //Delete all affected rules
        ruleRepository.delete(affectedRules);
    }
}