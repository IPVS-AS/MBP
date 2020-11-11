package org.citopt.connde.domain.component;

import org.citopt.connde.repository.ActuatorRepository;
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

        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "componentType", "component.componentType.empty",
                "The component type cannot be empty!");
        
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "device", "component.device.empty",
                "The device cannot be empty!");

        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "operator", "component.operator.empty",
                "The operator cannot be empty!");
    }
}
