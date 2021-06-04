package de.ipvs.as.mbp.util.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;

/**
 * Validation Annotation to check if an Integer is either unset or in the TCP / UDP Port range
 *
 * @author Christian Mueller
 */
@Target({ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = OptionalPortValidator.class)
@Documented
public @interface OptionalPort {
}
