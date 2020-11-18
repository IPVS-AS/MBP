package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.repository.TestDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;


@Component
public class TestDetailsValidator implements Validator {

    private static TestDetailsRepository repository;
    @Autowired
    public void setTypeRepository(TestDetailsRepository testDetailsRepository) {
        System.out.println("autowiring type to TypeValidator");
        TestDetailsValidator.repository = testDetailsRepository;
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


    public void validate(TestDetails test , Errors errors) {

        // Check if Testname is empty
        ValidationUtils.rejectIfEmptyOrWhitespace(
                errors, "name", "component.name.empty",
                "The name cannot be empty!");



        // Check if rule choice is empty
        if (test.getRules() == null) {
            errors.reject("rules", new String[]{"component.rules.empty"},
                    "At least one rule must be selected!");
        }

        /**
        // Check if the configuration for the sensor-simulator is empty
        if (test.getConfig() == null || test.getConfig().isEmpty()) {
            errors.rejectValue("config", "component.config.empty",
                    "Config must be defined.");
        }

**/

    }
}
