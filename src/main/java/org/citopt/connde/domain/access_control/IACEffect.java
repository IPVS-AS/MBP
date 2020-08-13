package org.citopt.connde.domain.access_control;

import java.util.List;

/**
 * An effect enables fine-granular decisions for access requests
 * by describing the outcome of decision. Effects can be to {@link IACValueLog value} logs.
 * 
 * @param <T> the data type of the {@link IACValueLog}.
 * @author Jakob Benz
 */
public interface IACEffect<T> {
	
	public String getName();
	
	/**
	 * Applies the effect to a given input value.
	 * 
	 * @param inputValue the input value.
	 * @return the value after the effect has been applied.
	 */
	public T applyToValue(T inputValue);
	
	/**
	 * Applies the effect to a given list of input values.
	 * 
	 * @param inputValues the {@link List} of input values.
	 * @return the list of values after the effect has been applied to each value.
	 */
	public List<T> applyToValues(List<T> inputValues);
	
	/**
	 * Applies the effect to a given input value log.
	 * 
	 * @param inputValueLog the input {@link IACValueLog value} log.
	 * @return the value log after the effect has been applied.
	 */
	public T applyToValueLog(IACValueLog<T> inputValueLog);
	
	/**
	 * Applies the effect to a given list of value logs.
	 * 
	 * @param inputValuesLog the {@link List} of input {@link IACValueLog value} logs.
	 * @return the list of value logs after the effect has been applied to each value.
	 */
	public List<T> applyToValueLogs(List<IACValueLog<T>> inputValuesLog);

}
