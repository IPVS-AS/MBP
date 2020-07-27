package org.citopt.connde.domain.access_control;

import java.text.DecimalFormat;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.Nonnull;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * This {@link IACModifyingEffect effect} manipulates the accuracy of a {@link Double} value.
 * 
 * @author Jakob Benz
 */
public class ACDoubleAccuracyEffect implements IACModifyingEffect<Double> {
	
	/**
	 * The accuracy the application of this effect will result in. For example,
	 * an accuracy of 10 with an input of 87.5 would result in 90.
	 */
	@Nonnull
	private final double accuracy;
	
	
	/**
	 * The number of decimal digits to keep when rounding the result.
	 * If -1 is specified, the original (result) value will be used.
	 */
	@Nonnull
	@Min(-1)
	@Max(10)
	private final int precision;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param accuracy
	 * @param precision
	 */
	public ACDoubleAccuracyEffect(double accuracy, int precision) {
		this.accuracy = accuracy;
		this.precision = precision;
	}
	
	// - - -
	
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
