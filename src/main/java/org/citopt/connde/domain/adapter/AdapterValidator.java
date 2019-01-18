package org.citopt.connde.domain.adapter;

import org.citopt.connde.domain.adapter.parameters.Parameter;
import org.citopt.connde.repository.AdapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author rafaelkperes
 */
@Component
public class AdapterValidator implements Validator {
    //Max length of parameter units
    private static final int PARAMETER_UNIT_MAX_LENGTH = 20;

    static AdapterRepository repository;

    @Autowired
    public void setTypeRepository(AdapterRepository adapterRepository) {
        System.out.println("autowiring type to TypeValidator");
        AdapterValidator.repository = adapterRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Adapter.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Adapter adapter = (Adapter) o;

        validate(adapter, errors);
    }

    public void validate(Adapter adapter, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "adapter.name.empty",
                "The name cannot be empty!");

        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "description", "adapter.description.empty",
                "The description cannot be empty!");

        if (!adapter.hasRoutines()) {
            errors.rejectValue("routines", "adapter.routines.empty",
                    "Routine files must be provided.");
        }

        //Check parameters for validity
        Set<String> nameSet = new HashSet<>();
        for(Parameter parameter : adapter.getParameters()){
            String name = parameter.getName();
            //Check name
            if((name == null) || name.isEmpty()){
                errors.rejectValue("parameters", "adapter.parameters.empty",
                        "Parameter names must not be empty.");
                break;
            }
            //Check name for uniqueness
            if(nameSet.contains(name)){
                errors.rejectValue("parameters", "adapter.parameters.duplicate",
                        "Parameter names must be unique.");
                break;
            }
            //Check type
            if(parameter.getType() == null){
                errors.rejectValue("parameters", "adapter.parameters.untyped",
                        "Valid parameter types must be provided.");
                break;
            }
            //Check unit
            if((parameter.getUnit() != null) && (parameter.getUnit().length() > PARAMETER_UNIT_MAX_LENGTH)){
                errors.rejectValue("parameters", "adapter.parameters.invalid_unit",
                        "Units must not be longer than " + PARAMETER_UNIT_MAX_LENGTH + " characters.");
                break;
            }
            nameSet.add(name);
        }

        Adapter another;
        if ((another = repository.findByName(adapter.getName())) != null) {
            if (adapter.getId() == null
                    || !adapter.getId().equals(another.getId())) {
                errors.rejectValue("name", "type.name.duplicate",
                        "The name is already registered");
            }
        }
    }
}
