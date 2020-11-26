package org.citopt.connde.domain.user_entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citopt.connde.service.IDeleteValidator;

/**
 * Annotation used to declare MBP managed entities.
 * 
 * @author Jakob Benz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface MBPEntity {
	
	public Class<? extends IDeleteValidator<?>>[] deleteValidator() default {};
	
}
