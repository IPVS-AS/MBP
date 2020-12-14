package de.ipvs.as.mbp.domain.operator;

import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Creation validator for operator entities.
 */
@Service
public class OperatorCreateValidator implements ICreateValidator<Operator> {
    //Max length of parameter units
    private static final int PARAMETER_UNIT_MAX_LENGTH = 20;

    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    @Override
    public void validateCreatable(Operator entity) {
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


        //Check whether routines were provided
        if (!entity.hasRoutines()) {
            exception.addInvalidField("routines", "Script files must be provided.");
        }

        //Check unit for validity
        if (!Validation.isValidUnit(entity.getUnit())) {
            exception.addInvalidField("unit", "Unable to parse provided unit.");
        }

        //Check parameters for validity
        Set<String> nameSet = new HashSet<>();
        for (Parameter parameter : entity.getParameters()) {
            String name = parameter.getName();
            //Check name
            if ((name == null) || name.isEmpty()) {
                exception.addInvalidField("parameters", "Parameter names must not be empty.");
                break;
            }

            //Check name for uniqueness
            if (nameSet.contains(name)) {
                exception.addInvalidField("parameters", "Parameter names must be unique.");
                break;
            }

            //Check type
            if (parameter.getType() == null) {
                exception.addInvalidField("parameters", "Parameter type is invalid.");
                break;
            }

            //Check unit
            if ((parameter.getUnit() != null) && (parameter.getUnit().length() > PARAMETER_UNIT_MAX_LENGTH)) {
                exception.addInvalidField("parameters", "Units must not be longer than " +
                        PARAMETER_UNIT_MAX_LENGTH + " characters.");
                break;
            }
            nameSet.add(name);
        }

        //Throw exception if there are invalid fields
        if (exception.hasInvalidFields()) {
            throw exception;
        }
    }
}
