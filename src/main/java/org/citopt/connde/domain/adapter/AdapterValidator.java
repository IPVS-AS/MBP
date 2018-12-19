package org.citopt.connde.domain.adapter;

import org.citopt.connde.repository.AdapterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 * @author rafaelkperes
 */
@Component
public class AdapterValidator implements Validator {

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
