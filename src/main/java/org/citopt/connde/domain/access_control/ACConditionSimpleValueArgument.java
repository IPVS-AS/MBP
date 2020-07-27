package org.citopt.connde.domain.access_control;

/**
 * A simple value argument intended to use with {@link ACSimpleCondition}.
 * 
 * @param <T> the data type of this simple value argument.
 * @author Jakob Benz
 */
public class ACConditionSimpleValueArgument<T extends Comparable<T>> implements IACConditionValueArgument<T> {
	
	/**
	 * The value of this argument.
	 */
	private T value;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param value the value of this argument.
	 */
	public ACConditionSimpleValueArgument(T value) {
		this.value = value;
	}
	
	// - - -

	@Override
	public T getValue() {
		return value;
	}

	public ACConditionSimpleValueArgument<T> setValue(T value) {
		this.value = value;
		return this;
	}
	
}
