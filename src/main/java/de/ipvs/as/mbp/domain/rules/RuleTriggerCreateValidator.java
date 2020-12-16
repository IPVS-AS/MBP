package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.RuleTriggerRepository;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQueryValidation;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creation validator for rule trigger entities.
 */
@Service
public class RuleTriggerCreateValidator implements ICreateValidator<RuleTrigger> {

    private static CEPTriggerService triggerService;

    /**
     * Sets the CEPTriggerService and the RuleTriggerRepository (autowired).
     *
     * @param triggerService        The CEPTriggerService to set
     * @param ruleTriggerRepository The RuleTriggerRepository to set
     */
    @Autowired
    public void setAutowired(CEPTriggerService triggerService, RuleTriggerRepository ruleTriggerRepository) {
        RuleTriggerCreateValidator.triggerService = triggerService;
    }

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(RuleTrigger entity) {
        //Sanity check
        if (entity == null) {
            throw new EntityValidationException("The entity is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(entity.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check query
        if (Validation.isNullOrEmpty(entity.getName())) {
            exception.addInvalidField("query", "The query string must not be empty.");
            throw exception;
        }

        // Validate trigger query
        CEPQueryValidation queryValidation = triggerService.isValidTriggerQuery(entity);

        // Is query not valid?
        if (!queryValidation.isValid()) {
            String errorMessage = "Invalid query.";

            // Check if an error message was provided
            if (queryValidation.hasErrorMessage()) {
                errorMessage = "Invalid query: " + queryValidation.getErrorMessage();
            }

            //Mark as invalid with returned error message
            exception.addInvalidField("query", errorMessage);
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
