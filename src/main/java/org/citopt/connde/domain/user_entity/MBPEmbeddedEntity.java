package org.citopt.connde.domain.user_entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.citopt.connde.repository.UserEntityRepository;

/**
 * TODO: Comment!
 * 
 * @author Jakob Benz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MBPEmbeddedEntity {
	
	public boolean cascadeDelete() default false;
	
}
