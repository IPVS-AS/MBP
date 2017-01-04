package org.citopt.sensmonqtt.domain.component;

import org.citopt.sensmonqtt.repository.ActuatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

/**
 *
 * @author rafaelkperes
 */
@org.springframework.stereotype.Component
public class ActuatorValidator implements Validator {

    static ActuatorRepository repository;

    @Autowired
    public void setComponentRepository(ActuatorRepository repository) {
        System.out.println("autowiring actuatorRepository to ActuatorValidator");
        ActuatorValidator.repository = repository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Actuator.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        Component component = (Component) o;

        validate(component, errors);
    }

    public void validate(Component component, Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "component.name.empty",
                "The name cannot be empty!");

        Component another;
        if ((another = repository.findByName(component.getName())) != null) {
            if (component.getId() == null
                    || !component.getId().equals(another.getId())) {
                errors.rejectValue("name", "component.name.duplicate",
                        "The name is already registered");
            }
        }
    }
}
