package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.service.rules.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Event handler for operations that are performed on rules.
 */
@Component
@RepositoryEventHandler
public class RuleEventHandler {

    @Autowired
    private RuleEngine ruleEngine;

    /**
     * Called, when a rule is supposed to be deleted. This method then takes care of disabling the rule
     * at the rule engine in an ordinary way.
     *
     * @param rule The rule that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeRuleDelete(Rule rule) {
        ruleEngine.disableRule(rule);
    }
}