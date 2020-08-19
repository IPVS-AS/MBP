package org.citopt.connde.domain.access_control;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 * Abstract base class for access-control effects, that, e.g., filter or modify
 * requested (sensor) data.
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractEffect<T> {
	
	/**
	 * The name of this effect.
	 */
	@NotEmpty
	private String name; // implicitly final due to omitted setter
	
	/**
	 * The list of parameters this effect requires to be applied.
	 */
	private Map<String, String> parameters = new HashMap<>();
	
	private ACEffectType type = getClass().getAnnotation(ACEffect.class).type();
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAbstractEffect() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this effect.
	 */
	public ACAbstractEffect(String name, Map<String, String> parameters) {
		this.name = name;
		this.parameters = parameters;
	}
	
	// - - -
	
	public String getName() {
		return name;
	}
	
	public ACAbstractEffect<T> setName(String name) {
		this.name = name;
		return this;
	}
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public ACAbstractEffect<T> setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}
	
	public ACEffectType getType() {
		return type;
	}
	
	// - - -

	/**
	 * TODO: Method comment.
	 * 
	 * @param inputValue
	 * @return
	 */
	public abstract T applyToValue(T inputValue);

	/**
	 * TODO: Method comment.
	 * 
	 * @param inputValueLog
	 * @return
	 */
	public abstract T applyToValueLog(IACValueLog<T> inputValueLog);
	
	public static void main(String[] args) {
		ACDoubleAccuracyEffect e = new ACDoubleAccuracyEffect();
		System.out.println(e.getType().getName());
		System.out.println(e.getType().getDescription());
		System.out.println(e.getType().getClazz());
	}
	
}
