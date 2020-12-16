package de.ipvs.as.mbp.domain.access_control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to mark classes as access-control effects.
 * 
 * @author Jakob Benz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ACEffect {

	/**
	 * The {@link ACEffectType} of this effect (required).
	 */
	public ACEffectType type();

}
