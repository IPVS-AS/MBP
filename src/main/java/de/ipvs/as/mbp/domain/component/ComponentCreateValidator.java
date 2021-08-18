package de.ipvs.as.mbp.domain.component;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Creation validator for component entities.
 */
@Service
public class ComponentCreateValidator implements ICreateValidator<Component> {

    @Autowired
    SensorRepository sensorRepository;

    @Autowired
    ActuatorRepository actuatorRepository;

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(Component entity) {
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


        //Check component type
        if (Validation.isNullOrEmpty(entity.getComponentType())) {
            exception.addInvalidField("componentType", "The component type must not be empty.");
        }

        //Check device
        if (entity.getDevice() == null) {
            exception.addInvalidField("device", "A device must be selected.");
        }

        //Check device
        if (entity.getOperator() == null) {
            exception.addInvalidField("operator", "An operator must be selected.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
