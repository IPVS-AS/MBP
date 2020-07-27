package org.citopt.connde.domain.access_control;

import java.lang.annotation.Annotation;

/**
 * Exception class used to indicate that an {@link Annotation} is required
 * and has not been specified for a certain class, method, field, ... .
 * 
 * @author Jakob Benz
 */
public class MissingAnnotationException extends IllegalArgumentException {

	private static final long serialVersionUID = 4645277018894382897L;

	/**
	 * Constructs a {@code MissingAnnotationException} for a given detail message.
	 * 
	 * @param message the detail message.
	 */
	public MissingAnnotationException(String message) {
		super(message);
	}
	
	/**
	 * Constructs a {@code MissingAnnotationException} for a given annotation type
	 * and target element.
	 * 
	 * @param annotationClass the annotation class.
	 * @param target the target element (description) as {@code String}.
	 */
	public MissingAnnotationException(Class<?> annotationClass, String target) {
		super("Annotation '" + annotationClass.getName() + "' required but not specified for '" + target + "'.");
	}

}
