package org.citopt.connde.domain.access_control;

import java.util.Map;

import org.citopt.connde.domain.user.User;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This {@link IACModifyingEffect effect} manipulates the accuracy of a {@link Double} value.
 * 
 * @author Jakob Benz
 */
@ACEffect(type = ACEffectType.LOCATION_ACCURACY_EFFECT)
@Document
public class ACLocationAccuracyEffect extends ACAbstractEffect {
	
	public static final String PARAM_KEY_ACCURACY = "accuracy";
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACLocationAccuracyEffect() {
		super();
	}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this effect.
	 * @param description the description of this effect.
	 * @param parameters the list of parameters this required to be applied.
	 * @param owner the {@link User} that owns this effect.
	 */
	public ACLocationAccuracyEffect(String name, String description, Map<String, String> parameters, User owner) {
		super(name, description, parameters, owner);
	}
	
	// - - -

	public Location applyToValue(Location inputValue) {
		return apply(inputValue);
	}

	public Location applyToValueLog(IACValueLog<Location> inputValueLog) {
		return apply(inputValueLog.getValue());
	}
	
	// - - -

	/**
	 * Applies this effect to a given input value.
	 * 
	 * @param inputValue the input {@link Double} value.
	 * @return the rounded and formatted value.
	 */
	private Location apply(Location inputValue) {
		// TODO: Implement
//		return round(Math.round(inputValue / getAccuracy()) * getAccuracy());
		return inputValue;
	}
	
//	/**
//	 * Rounds and formats a given double value using a given precision.
//	 * 
//	 * @param value the input {@link Double} value.
//	 * @return the rounded and formatted value.
//	 */
//	private Double round(Double value) {
//		String format = "0." + IntStream.range(0, getPrecision()).mapToObj(i -> "0").collect(Collectors.joining());
//		DecimalFormat df = new DecimalFormat(format);
//		return Double.parseDouble(df.format(value));
//	}
	
	private double getAccuracy() {
		return Double.parseDouble(getParameters().get(PARAM_KEY_ACCURACY));
	}

}
