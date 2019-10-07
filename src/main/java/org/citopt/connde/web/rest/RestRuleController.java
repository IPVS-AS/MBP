package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.rules.RuleActionType;
import org.citopt.connde.repository.RuleActionRepository;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.rules.RuleEngine;
import org.citopt.connde.service.rules.RuleExecutor;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller that exposes methods for the purpose of managing rules.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestRuleController {

    @Autowired
    private RuleActionRepository ruleActionRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private RuleExecutor ruleExecutor;

    @GetMapping(value = "/rule-actions/types")
    public ResponseEntity<RuleActionType[]> getRuleActionTypes() {
        //Get all available action types
        RuleActionType[] actionTypes = RuleActionType.values();
        return new ResponseEntity<>(actionTypes, HttpStatus.OK);
    }

    @PostMapping(value = "/rules/enable/{id}")
    public ResponseEntity<ActionResponse> enableRule(@PathVariable(value = "id") String ruleId) {
        //Get rule from repository
        Rule rule = ruleRepository.findOne(ruleId);

        //Check if rule was found
        if (rule == null) {
            //Not found, return error message
            ActionResponse response = new ActionResponse(false, "The rule does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Remembers whether enabling was successful
        boolean success = true;

        //Enable rule if necessary
        if (!rule.isEnabled()) {
            success = ruleEngine.enableRule(rule);
        }

        //Check for failure
        if (!success) {
            //Return error message
            ActionResponse response = new ActionResponse(false,
                    "Do all required components still exist?");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //Return success message
        ActionResponse response = new ActionResponse(true, "success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/rules/disable/{id}")
    public ResponseEntity<ActionResponse> disableRule(@PathVariable(value = "id") String ruleId) {
        //Get rule from repository
        Rule rule = ruleRepository.findOne(ruleId);

        //Check if rule was found
        if (rule == null) {
            //Not found, return error message
            ActionResponse response = new ActionResponse(false, "The rule does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Disable rule if necessary
        if (rule.isEnabled()) {
            ruleEngine.disableRule(rule);
        }

        //Return success message
        ActionResponse response = new ActionResponse(true, "success");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping(value = "/rule-actions/test/{id}")
    public ResponseEntity<ActionResponse> testRuleAction(@PathVariable(value = "id") String actionId) {
        //Get rule action from repository
        RuleAction ruleAction = ruleActionRepository.findOne(actionId);

        //Check if rule was found
        if (ruleAction == null) {
            //Not found, return error message
            ActionResponse response = new ActionResponse(false, "The rule action does not exist.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        //Test action
        boolean result = ruleExecutor.testRuleAction(ruleAction);

        //Return result
        return new ResponseEntity<>(new ActionResponse(result), HttpStatus.OK);
    }
}
