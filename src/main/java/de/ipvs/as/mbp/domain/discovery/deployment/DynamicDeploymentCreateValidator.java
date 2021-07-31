package de.ipvs.as.mbp.domain.discovery.deployment;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Creation validator for {@link DynamicDeployment} entities.
 */
@Service
public class DynamicDeploymentCreateValidator implements ICreateValidator<DynamicDeployment> {
    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param dynamicDeployment The entity to validate on creation
     */
    @Override
    public void validateCreatable(DynamicDeployment dynamicDeployment) {
        //Null check
        if (dynamicDeployment == null) {
            throw new EntityValidationException("The dynamic deployment is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create dynamic deployment, because some fields are invalid.");

        //Check name
        if ((dynamicDeployment.getName() == null) || (dynamicDeployment.getName().isEmpty())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check operator
        if (dynamicDeployment.getOperator() == null) {
            exception.addInvalidField("operator", "An operator must be selected.");
        }

        //Check device template
        if (dynamicDeployment.getDeviceTemplate() == null) {
            exception.addInvalidField("deviceTemplate", "A device template must be selected.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
