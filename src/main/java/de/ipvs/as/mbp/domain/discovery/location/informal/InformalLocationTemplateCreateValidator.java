package de.ipvs.as.mbp.domain.discovery.location.informal;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

/**
 * Creation validator for {@link InformalLocationTemplate} entities.
 */
@Service
public class InformalLocationTemplateCreateValidator implements ICreateValidator<InformalLocationTemplate> {

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param informalLocationTemplate The entity to validate on creation
     */
    @Override
    public void validateCreatable(InformalLocationTemplate informalLocationTemplate) {
        //Sanity check
        if (informalLocationTemplate == null) {
            throw new EntityValidationException("The location template is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create location template, because some fields are invalid.");

        //Check name
        if (Validation.isNullOrEmpty(informalLocationTemplate.getName())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check operator
        if (informalLocationTemplate.getOperator() == null) {
            exception.addInvalidField("operator", "The operator must not be empty.");
        }

        //Check match
        if (Validation.isNullOrEmpty(informalLocationTemplate.getMatch())) {
            exception.addInvalidField("match", "The match must not be empty.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
