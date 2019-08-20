package org.citopt.connde.domain.rules;

import org.citopt.connde.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.List;


/**
 * Validator for rule objects.
 */
@org.springframework.stereotype.Component
public class RuleValidator implements Validator {

    //Repository for rules
    private static RuleRepository ruleRepository;

    /**
     * Sets the rule repository (autowired).
     *
     * @param ruleRepository The rule repository to set
     */
    @Autowired
    public void setRuleRepository(RuleRepository ruleRepository) {
        RuleValidator.ruleRepository = ruleRepository;
    }

    /**
     * Checks whether the validator can be applied to objects of a given class. However, this validator can
     * only be applied to rule objects.
     *
     * @param type The class to check
     * @return True, if the validator can be applied to objects of this class; false otherwise
     */
    @Override
    public boolean supports(Class<?> type) {
        return Rule.class.equals(type);
    }

    /**
     * Validates the fields of a rule object and adds error messages to the errors object accordingly.
     *
     * @param o      The object to validate
     * @param errors The errors object to add the error messages to
     */
    @Override
    public void validate(Object o, Errors errors) {
        //Cast to rule
        Rule rule = (Rule) o;

        //Check if name was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "rule.name.empty",
                "The name must not be empty.");

        //Check if name is unique
        Rule anotherRule = ruleRepository.findByName(rule.getName());
        if (anotherRule != null) {
            errors.rejectValue("name", "rule.name.duplicate",
                    "The name is already registered.");
        }

        //Check if a rule trigger was provided
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "trigger", "rule.trigger.empty",
                "A rule trigger needs to be selected.");


        //Check if at least one rule action was provided
        List<RuleAction> ruleActions = rule.getActions();
        if ((ruleActions == null) || ruleActions.isEmpty()) {
            errors.rejectValue("actions", "rule.actions.empty",
                    "At least one rule action needs to be selected.");
        }
    }
}
