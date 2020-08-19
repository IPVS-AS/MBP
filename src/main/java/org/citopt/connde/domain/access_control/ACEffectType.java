package org.citopt.connde.domain.access_control;

/**
 * Enumeration for all available access-control policy effects.
 * 
 * @author Jakob Benz
 */
public enum ACEffectType {
	
	DOUBLE_ACCURACY_EFFECT("Double Accuracy Modification", "This effect allows manipulating the accuracy and precision of a double value.", ACDoubleAccuracyEffect.class),
	LOCATION_ACCURACY_EFFECT("Location Accuracy Modification", "This effect allows manipulating the accuary of a location value.", ACLocationAccuracyEffect.class);
	
	// - - -
	
	private String name;
	private String description;
	private Class<? extends ACAbstractEffect<?>> clazz;
	
	// - - -
	
	private ACEffectType(String name, String description, Class<? extends ACAbstractEffect<?>> clazz) {
		this.name = name;
		this.description = description;
		this.clazz = clazz;
	}
	
	// - - -
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Class<? extends ACAbstractEffect<?>> getClazz() {
		return clazz;
	}

}
