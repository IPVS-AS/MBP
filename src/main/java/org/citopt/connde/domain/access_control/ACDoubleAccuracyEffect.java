package org.citopt.connde.domain.access_control;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * This {@link IACModifyingEffect effect} manipulates the accuracy of a {@link Double} value.
 * 
 * @author Jakob Benz
 */
@Document
public class ACDoubleAccuracyEffect implements IACModifyingEffect<Double> {
	
	/**
	 * The name of this effect.
	 */
	@NotEmpty
	private String name; // implicitly final due to omitted setter
	
	/**
	 * The accuracy the application of this effect will result in. For example,
	 * an accuracy of 10 with an input of 87.5 would result in 90.
	 */
	@Nonnull
	private double accuracy; // implicitly final due to omitted setter
	
	
	/**
	 * The number of decimal digits to keep when rounding the result.
	 * If -1 is specified, the original (result) value will be used.
	 */
	@Nonnull
	@Min(-1)
	@Max(10)
	private int precision; // implicitly final due to omitted setter
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACDoubleAccuracyEffect() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this effect.
	 * @param accuracy the accuracy the application of this effect will result in.
	 * @param precision the number of decimal digits to keep when rounding the result.
	 */
	public ACDoubleAccuracyEffect(String name, double accuracy, int precision) {
		this.name = name;
		this.accuracy = accuracy;
		this.precision = precision;
	}
	
	// - - -
	
	@Override
	public String getName() {
		return name;
	}
	
	public double getAccuracy() {
		return accuracy;
	}
	
	public int getPrecision() {
		return precision;
	}
	
	// - - -

	@Override
	public Double applyToValue(Double inputValue) {
		return apply(inputValue);
	}

	@Override
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
		return round(Math.round(inputValue / accuracy) * accuracy);
	}
	
	/**
	 * Rounds and formats a given double value using a given precision.
	 * 
	 * @param value the input {@link Double} value.
	 * @return the rounded and formatted value.
	 */
	private Double round(Double value) {
		String format = "0." + IntStream.range(0, precision).mapToObj(i -> "0").collect(Collectors.joining());
		DecimalFormat df = new DecimalFormat(format);
		return Double.parseDouble(df.format(value));
	}

}
