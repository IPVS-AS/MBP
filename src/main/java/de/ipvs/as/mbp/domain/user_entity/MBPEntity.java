package de.ipvs.as.mbp.domain.user_entity;

import de.ipvs.as.mbp.service.validation.ICreateValidator;
import de.ipvs.as.mbp.service.validation.IDeleteValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to declare MBP managed entities.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface MBPEntity {
    Class<? extends ICreateValidator<?>>[] createValidator() default {};

    Class<? extends IDeleteValidator<?>>[] deleteValidator() default {};

}
