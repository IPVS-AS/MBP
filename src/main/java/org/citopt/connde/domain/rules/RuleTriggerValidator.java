package org.citopt.connde.domain.rules;

import org.citopt.connde.repository.RuleTriggerRepository;
import org.citopt.connde.service.cep.engine.core.queries.CEPQueryValidation;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


/**
 * Validator for rule trigger objects.
 */
@org.springframework.stereotype.Component
public class RuleTriggerValidator implements Validator {

    private static CEPTriggerService triggerService;

    private static RuleTriggerRepository ruleTriggerRepository;

    /**
     * Sets the CEPTriggerService and the RuleTriggerRepository (autowired).
     *
     * @param triggerService        The CEPTriggerService to set
     * @param ruleTriggerRepository The RuleTriggerRepository to set
     */
    @Autowired
    public void setAutowired(CEPTriggerService triggerService, RuleTriggerRepository ruleTriggerRepository) {
        RuleTriggerValidator.triggerService = triggerService;
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

        //Stop in case there are already problems with the query
        if (errors.hasFieldErrors("query")) {
            return;
        }

        //Validate trigger query
        CEPQueryValidation queryValidation = triggerService.isValidTriggerQuery(ruleTrigger);

        //Is query not valid?
        if (!queryValidation.isValid()) {
            String errorMessage = "Invalid query.";

            //Check if an error message was provided
            if (queryValidation.hasErrorMessage()) {
                errorMessage = "Invalid query: " + queryValidation.getErrorMessage();
            }

            //Reject
            errors.rejectValue("query", "ruleTrigger.query.invalid", errorMessage);
        }
    }
}
