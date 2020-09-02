package org.citopt.connde.domain.access_control;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.citopt.connde.domain.access_control.jquerybuilder.JQBRule;

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
	 * No-args constructor.
	 */
	public ACConditionSimpleValueArgument() {}
	
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
	
	// - - -
	
	public static ACConditionSimpleValueArgument<?> basedOn(JQBRule rule) {
		switch (rule.getType().toLowerCase()) {
		case "string": return new ACConditionSimpleValueArgument<>(rule.getValue());
		case "integer": return new ACConditionSimpleValueArgument<>(Integer.parseInt(rule.getValue()));
		case "double": return new ACConditionSimpleValueArgument<>(Double.parseDouble(rule.getValue()));
		case "date": return new ACConditionSimpleValueArgument<>(LocalDate.parse(rule.getValue()));
		case "time": return new ACConditionSimpleValueArgument<>(LocalTime.parse(rule.getValue()));
		case "datetime": return new ACConditionSimpleValueArgument<>(LocalDateTime.parse(rule.getValue()));
		case "boolean": return new ACConditionSimpleValueArgument<>(Boolean.parseBoolean(rule.getValue()));
		default: return null;
		}
	}
	
}
