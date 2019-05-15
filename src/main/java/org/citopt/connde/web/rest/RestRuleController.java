package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.rules.RuleEngine;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller that exposes methods for the purpose of managing rules.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestRuleController {

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleEngine ruleEngine;

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

        //Enable rule if necessary
        if (!rule.isEnabled()) {
            ruleEngine.enableRule(rule);
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
}
