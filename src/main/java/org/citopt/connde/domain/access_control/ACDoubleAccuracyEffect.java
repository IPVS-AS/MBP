package org.citopt.connde.domain.access_control;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.citopt.connde.domain.user.User;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.server.core.Relation;

/**
 * This {@link IACModifyingEffect effect} manipulates the accuracy of a {@link Double} value.
 * 
 * @author Jakob Benz
 */
@Document
@ACEffect(type = ACEffectType.NUMERIC_ACCURACY_MODIFICATION)
@Relation(collectionRelation = "policy-effects", itemRelation = "policy-effects")
public class ACDoubleAccuracyEffect extends ACAbstractEffect {
	
	public static final String PARAM_KEY_ACCURACY = "accuracy";
	public static final String PARAM_KEY_PRECISION = "precision";
	
//	/**
//	 * The accuracy the application of this effect will result in. For example,
//	 * an accuracy of 10 with an input of 87.5 would result in 90.
//	 */
//	@Nonnull
//	private double accuracy; // implicitly final due to omitted setter
//	
//	
//	/**
//	 * The number of decimal digits to keep when rounding the result.
//	 * If -1 is specified, the original (result) value will be used.
//	 */
//	@Nonnull
//	@Min(-1)
//	@Max(10)
//	private int precision; // implicitly final due to omitted setter
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACDoubleAccuracyEffect() {
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
	public ACDoubleAccuracyEffect(String name, String description, Map<String, String> parameters, User owner) {
		super(name, description, parameters, owner);
	}
	
	// - - -
	
	public Double applyToValue(Double inputValue) {
		return apply(inputValue);
	}

	public Double applyToValueLog(IACValueLog<Double> inputValueLog) {
		return apply(inputValueLog.getValue());
	}
	
	// - - -

	/**
	 * Applies this effect to a given input value.
	 * 
	 * @param inputValue the input {@link Double} value.
	 * @return the rounded and formatted value.
	 */
	private Double apply(Double inputValue) {
		return round(Math.round(inputValue / getAccuracy()) * getAccuracy());
	}
	
	/**
	 * Rounds and formats a given double value using a given precision.
	 * 
	 * @param value the input {@link Double} value.
	 * @return the rounded and formatted value.
	 */
	private Double round(Double value) {
		String format = "0." + IntStream.range(0, getPrecision()).mapToObj(i -> "0").collect(Collectors.joining());
		DecimalFormat df = new DecimalFormat(format);
		return Double.parseDouble(df.format(value));
	}
	
	private double getAccuracy() {
		return Double.parseDouble(getParameters().get(PARAM_KEY_ACCURACY));
	}
	
	private int getPrecision() {
		return Integer.parseInt(getParameters().get(PARAM_KEY_PRECISION));
	}

}
