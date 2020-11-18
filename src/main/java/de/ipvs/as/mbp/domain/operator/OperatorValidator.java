package de.ipvs.as.mbp.domain.operator;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import de.ipvs.as.mbp.repository.OperatorRepository;
import de.ipvs.as.mbp.domain.operator.parameters.Parameter;
import de.ipvs.as.mbp.util.Validation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author rafaelkperes
 */
@Component
public class OperatorValidator implements Validator {
    //Max length of parameter units
    private static final int PARAMETER_UNIT_MAX_LENGTH = 20;

    private static OperatorRepository repository;

    @Autowired
    public void setTypeRepository(OperatorRepository operatorRepository) {
        System.out.println("autowiring type to TypeValidator");
        OperatorValidator.repository = operatorRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Operator.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Operator operator = (Operator) o;

        validate(operator, errors);
    }

    public void validate(Operator operator, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "operator.name.empty",
                "The name cannot be empty!");

        //Check whether routines were provided
        if (!operator.hasRoutines()) {
            errors.rejectValue("routines", "operator.routines.empty",
                    "Routine files must be provided.");
        }

        //Check unit for validity
        if (!Validation.isValidUnit(operator.getUnit())) {
            errors.rejectValue("unit", "operator.unit.invalid",
                    "Unable to parse unit specification.");
        }

        //Check parameters for validity
        Set<String> nameSet = new HashSet<>();
        for (Parameter parameter : operator.getParameters()) {
            String name = parameter.getName();
            //Check name
            if ((name == null) || name.isEmpty()) {
                errors.rejectValue("parameters", "operator.parameters.empty",
                        "Parameter names must not be empty.");
                break;
            }
            //Check name for uniqueness
            if (nameSet.contains(name)) {
                errors.rejectValue("parameters", "operator.parameters.duplicate",
                        "Parameter names must be unique.");
                break;
            }
            //Check type
            if (parameter.getType() == null) {
                errors.rejectValue("parameters", "operator.parameters.untyped",
                        "Valid parameter types must be provided.");
                break;
            }
            //Check unit
            if ((parameter.getUnit() != null) && (parameter.getUnit().length() > PARAMETER_UNIT_MAX_LENGTH)) {
                errors.rejectValue("parameters", "operator.parameters.invalid_unit",
                        "Units must not be longer than " + PARAMETER_UNIT_MAX_LENGTH + " characters.");
                break;
            }
            nameSet.add(name);
        }

        Optional<Operator> another = repository.findByName(operator.getName());
        if (another.isPresent()) {
            if (operator.getId() == null || !operator.getId().equals(another.get().getId())) {
                errors.rejectValue("name", "type.name.duplicate",
                        "The name is already registered");
            }
        }
    }
}
