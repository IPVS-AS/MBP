package de.ipvs.as.mbp.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class OptionalPortValidator implements ConstraintValidator<OptionalPort, Integer> {
    @Override
    public boolean isValid(Integer port, ConstraintValidatorContext constraintValidatorContext) {
        // Ensure the Port is either unset (i.e. null) or in the valid range between 1 and 2^16 - 1
        return port == null || (port >= 1 && port <= 65535);
    }
}
