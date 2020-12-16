package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creation validator for rule entities.
 */
@Service
public class RuleCreateValidator implements ICreateValidator<Rule> {

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(Rule entity) {
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

        //Check trigger
        if (entity.getTrigger() == null) {
            exception.addInvalidField("trigger", "A rule trigger needs to be selected.");
        }

        // Check if at least one rule action was provided
        List<RuleAction> ruleActions = entity.getActions();
        if ((ruleActions == null) || ruleActions.isEmpty()) {
            exception.addInvalidField("actions", "At least one rule action needs to be selected.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
