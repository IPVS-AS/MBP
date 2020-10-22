package org.citopt.connde.domain.user_entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citopt.connde.repository.UserEntityRepository;

/**
 * Annotation used to declare MBP managed entities and to indicate the concrete repository implementation.
 * 
 * @author Jakob Benz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface MBPEntity {
	
	public Class<?> repository() default UserEntityRepository.class;
	
}
