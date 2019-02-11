package org.citopt.connde.domain.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.citopt.connde.repository.SensorRepository;

/**
 *
 * @author rafaelkperes
 */
@org.springframework.stereotype.Component
public class SensorValidator implements Validator {

    static SensorRepository repository;

    @Autowired
    public void setComponentRepository(SensorRepository componentRepository) {
        System.out.println("autowiring sensorRepository to SensorValidator");
        SensorValidator.repository = componentRepository;
    }

    @Override
    public boolean supports(Class<?> type) {
        return Sensor.class.isAssignableFrom(type);
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
                errors, "adapter", "component.adapter.empty",
                "The adapter cannot be empty!");
        
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
