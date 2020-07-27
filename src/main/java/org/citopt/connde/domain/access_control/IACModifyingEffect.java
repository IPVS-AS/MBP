package org.citopt.connde.domain.access_control;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An effect that modifies a given input value, e.g., by
 * reducing its accuracy.
 *
 * @param <T> the data type of the {@link IACValueLog}.
 * @author Jakob Benz
 */
public interface IACModifyingEffect<T> extends IACEffect<T> {
	
	/* (non-Javadoc)
	 * @see org.citopt.connde.domain.access_control.IEffect#applyToValues(java.util.List)
	 */
	@Override
	default public List<T> applyToValues(List<T> inputValues) {
		return inputValues.stream().map(this::applyToValue).collect(Collectors.toList());
	}
	
	/* (non-Javadoc)
	 * @see org.citopt.connde.domain.access_control.IEffect#applyToValueLogs(java.util.List)
	 */
	@Override
	default List<T> applyToValueLogs(List<IACValueLog<T>> inputValuesLog) {
		return inputValuesLog.stream().map(this::applyToValueLog).collect(Collectors.toList());
	}
	
}
