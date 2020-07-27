package org.citopt.connde.domain.access_control;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Enumeration for different functions that can be used to compare two arguments.
 * 
 * @author Jakob Benz
 */
public enum ACArgumentFunction {
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * two arguments are equal.
	 */
	EQUALS(compareResult -> compareResult == 0),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * two arguments are <u>not</u> equal.
	 */
	NOT_EQUALS(compareResult -> compareResult != 0),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is less than the second argument.
	 */
	LESS_THAN(compareResult -> compareResult < 0),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is less than or equal to the second argument.
	 */
	LESS_THAN_OR_EQUAL_TO(compareResult -> compareResult <= 0),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is greater than the second argument.
	 */
	GREATER_THAN(compareResult -> compareResult > 0),
	
	/**
	 * Function that evaluates to {@code true}, if and only if
	 * the first argument is greater than or equal to the second argument.
	 */
	GREATER_THAN_OR_EQUAL_TO(compareResult -> compareResult >= 0);
	
	// - - -
	
	/**
	 * The predicate used to evaluate the result of the call to {@link Comparable#compareTo(Object)}.
	 */
	private Predicate<Integer> evaluationPredicate;
	
	/**
	 * All-args constructor.
	 * 
	 * @param evaluationPredicate The {@link Predicate} used to evaluate
	 * 		  the result of the call to {@link Comparable#compareTo(Object)}.
	 */
	private ACArgumentFunction(Predicate<Integer> evaluationPredicate) {
		this.evaluationPredicate = evaluationPredicate;
	}
	
	public Predicate<Integer> getEvaluationPredicate() {
		return evaluationPredicate;
	}
	
	// - - -
	
	/**
	 * Convenience function to directly apply this argument function to
	 * the given arguments.
	 * 
	 * @param left the left side argument.
	 * @param right the right side argument.
	 * @return {@code true} if and only if the predicate associated with this
	 * 		   argument function holds; {@code false} otherwise.
	 */
	public <T extends Comparable<T>> boolean apply(T left, T right) {
		return apply(left.compareTo(right));
	}
	
	/**
	 * Convenience function to directly apply this argument function to
	 * the result of the call to {@link Comparable#compareTo(Object)}.
	 * 
	 * @param compareResult the result of the call to {@link Comparable#compareTo(Object)}.
	 * @return {@code true} if and only if the predicate associated with this
	 * 		   argument function holds; {@code false} otherwise.
	 */
	public boolean apply(int compareResult) {
		return evaluationPredicate.test(compareResult);
	}
	
	// - - -
	
	public static final List<ACArgumentFunction> ALL = Arrays.asList(EQUALS, NOT_EQUALS, LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO);

}
