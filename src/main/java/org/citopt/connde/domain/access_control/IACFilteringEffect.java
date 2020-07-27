package org.citopt.connde.domain.access_control;

/**
 * An effect that filters a list of given input values.
 *
 * @param <T> the data type of the {@link IACValueLog}.
 * @author Jakob Benz
 */
public interface IACFilteringEffect<T> extends IACEffect<T> {
	
	@Override
	default T applyToValue(T inputValue) {
		throw new UnsupportedOperationException("Filtering effects cannot be applied to single a single value.");
	}
	
	@Override
	default T applyToValueLog(IACValueLog<T> inputValueLog) {
		throw new UnsupportedOperationException("Filtering effects cannot be applied to single a single value log.");
	}

}
