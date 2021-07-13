package de.ipvs.as.mbp.domain.discovery.peripheral;

import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Creation validator for {@link DynamicPeripheral} entities.
 */
@Service
public class DynamicPeripheralCreateValidator implements ICreateValidator<DynamicPeripheral> {
    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param dynamicPeripheral The entity to validate on creation
     */
    @Override
    public void validateCreatable(DynamicPeripheral dynamicPeripheral) {
        //Null check
        if (dynamicPeripheral == null) {
            throw new EntityValidationException("The dynamic peripheral is invalid.");
        }

        //Create exception to collect invalid fields
        EntityValidationException exception = new EntityValidationException("Could not create dynamic peripheral, because some fields are invalid.");

        //Check name
        if ((dynamicPeripheral.getName() == null) || (dynamicPeripheral.getName().isEmpty())) {
            exception.addInvalidField("name", "The name must not be empty.");
        }

        //Check operator
        if (dynamicPeripheral.getOperator() == null) {
            exception.addInvalidField("operator", "An operator must be selected.");
        }

        //Check device template
        if (dynamicPeripheral.getOperator() == null) {
            exception.addInvalidField("deviceTemplate", "A device template must be selected.");
        }

        //Check request topics
        if ((dynamicPeripheral.getRequestTopics() == null) || dynamicPeripheral.getRequestTopics().isEmpty()) {
            exception.addInvalidField("requestTopics", "At least one request topic must be selected.");
        } else if (dynamicPeripheral.getRequestTopics().stream().anyMatch(Objects::isNull)) {
            exception.addInvalidField("requestTopics", "At least one request topic is invalid.");
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
