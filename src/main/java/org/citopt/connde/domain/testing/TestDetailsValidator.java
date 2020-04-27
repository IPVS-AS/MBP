package org.citopt.connde.domain.testing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


@Component
public class TestDetailsValidator implements Validator {

    @Autowired
    public void setTypeRepository() {
        System.out.println("autowiring type to TypeValidator");
    }

    @Override
    public boolean supports(Class<?> type) {
        return TestDetails.class.isAssignableFrom(type);
    }

    @Override
    public void validate(Object o, Errors errors) {
        TestDetails test = (TestDetails) o;

        validate(test, errors);
    }


    public void validate(TestDetails test, Errors errors) {
        // Check if name is empty
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "test.name.empty",
                "The name cannot be empty!");

        // Check if sensors are empty
        if (test.getSensor() == null || test.getSensor().isEmpty()) {
            errors.rejectValue("sensor", "test.sensor.empty",
                    "Sensor must be provided.");
        }

        // Check if rules is empty
        if (test.getRules() == null) {
            errors.rejectValue("rules", "test.rules.empty",
                    "Rules must be selected.");
        }

        // checks if the configuration for the sensor-simulator is empty
        if (test.getConfig() == null || test.getConfig().isEmpty()) {
            errors.rejectValue("config", "test.config.empty",
                    "Config must be defined.");
        }



    }
}
