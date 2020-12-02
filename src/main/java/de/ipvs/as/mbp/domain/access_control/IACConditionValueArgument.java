package de.ipvs.as.mbp.domain.access_control;

/**
 * A value condition argument, e.g., to be used with {@link ACSimpleCondition}.
 * 
 * @author Jakob Benz
 */
public interface IACConditionValueArgument<T extends Comparable<T>> extends IACConditionArgument {
	
	/**
	 * Returns the value of this condition argument.
	 * 
	 * @return the value of this condition argument.
	 */
	public T getValue();
	
}