package de.ipvs.as.mbp.domain.access_control;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks fields to be used as {@link ACAttribute attributes}.
 * 
 * @author Jakob Benz
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ACAttributeValue {
	
	/**
	 * The {@link ACAttributeKey} of the attribute.
	 * <p>
	 * If {@link #keyString()} is also specified, it will be preferably used.
	 */
	public ACAttributeKey key() default ACAttributeKey.NULL;
	
	/**
	 * The key of the attribute as {@code String} if no corresponding {@link ACAttributeKey}
	 * is available. Defaults to the name of the field this annotation has been specified for.
	 * <p>
	 * If {@link #key()} is also specified, it will be ignored in favor of this.
	 */
	public String keyString() default "";
	
	/**
	 * The path to the field with the actual attribute value in case
	 * the attribute value is not the field value itself but rather
	 * is a nested field somewhere in the object hierarchy.
	 * <p>
	 * The path must contain the field name (exactly) for each step
	 * (the field this annotation is specified on must <b>not</b> be included).
	 * If there are multiple steps, '.' must be used as separator.
	 * <p>
	 * If {@link #key()} is also specified, this path will be used preferable
	 * and the path in the {@link ACAttributeKey} will be ignored.
	 */
//	public String valueLookupPath() default "";

}
