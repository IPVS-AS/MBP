package org.citopt.connde.domain.rules;

import org.citopt.connde.repository.RuleTriggerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * Validator for rule trigger objects.
 */
@org.springframework.stereotype.Component
public class RuleTriggerValidator implements Validator {

    //Repository for rule triggers
    private static RuleTriggerRepository ruleTriggerRepository;

    /**
     * Sets the rule trigger repository (autowired).
     *
     * @param ruleTriggerRepository The rule trigger repository to set
     */
    @Autowired
    public void setRuleActionRepository(RuleTriggerRepository ruleTriggerRepository) {
        RuleTriggerValidator.ruleTriggerRepository = ruleTriggerRepository;
    }

    /**
     * Checks whether the validator can be applied to objects of a given class. However, this validator can
     * only be applied to rule trigger objects.
     *
     * @param type The class to check
     * @return True, if the validator can be applied to objects of this class; false otherwise
     */
    @Override
    public boolean supports(Class<?> type) {
        return RuleTrigger.class.equals(type);
    }

    /**
     * Validates the fields of a rule trigger object and adds error messages to the errors object accordingly.
     *
     * @param o      The object to validate
     * @param errors The errors object to add the error messages to
     */
    @Override
    public void validate(Object o, Errors errors) {
        //Cast to rule trigger
        RuleTrigger ruleTrigger = (RuleTrigger) o;

        //Check if name was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "ruleTrigger.name.empty",
                "The name must not be empty.");

        //Check if name is unique
        RuleTrigger anotherRuleTrigger = ruleTriggerRepository.findByName(ruleTrigger.getName());
        if (anotherRuleTrigger != null) {
            errors.rejectValue("name", "ruleTrigger.name.duplicate",
                    "The name is already registered.");
        }

        //Check if query string was provided (mandatory)
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "query", "ruleTrigger.query.empty",
                "The query string must not be empty.");
    }
}
