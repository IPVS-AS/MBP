package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.rules.execution.RuleActionExecutor;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/**
 * Creation validator for rule action entities.
 */
@Service
public class RuleActionCreateValidator implements ICreateValidator<RuleAction> {
    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(RuleAction entity) {

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

        // Check if type was provided
        RuleActionType actionType = entity.getType();
        if (actionType == null) {
            exception.addInvalidField("action", "A rule action type needs to be selected.");
            throw exception;
        }

        //Throw exception if there are invalid fields so far
        if (exception.hasInvalidFields()) {
            throw exception;
        }

        // If null is provided instead of a map, replace it with an empty map
        if (entity.getParameters() == null) {
            entity.setParameters(new HashMap<>());
        }

        // Get executor for this action type
        RuleActionExecutor executor = actionType.getExecutor();

        // Use executor to validate the provided parameters
        executor.validateParameters(entity.getParameters());
    }
}
