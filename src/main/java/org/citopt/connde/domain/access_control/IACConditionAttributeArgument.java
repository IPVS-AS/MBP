package org.citopt.connde.domain.access_control;

/**
 * An attribute condition argument, e.g., to be used with {@link ACSimpleCondition}.
 * 
 * @author Jakob Benz
 */
public interface IACConditionAttributeArgument<T extends Comparable<T>> extends IACConditionArgument {
	
	/**
	 * Returns the type of entity this condition argument is specified for.
	 * 
	 * @return the {@link ACEntityType} this condition argument is specified for.
	 */
	public ACEntityType getEntityType();
	
	/**
	 * Returns the {@link ACAttributeKey} of the attribute this condition argument is specified for.
	 * 
	 * @return the {@link ACAttributeKey} of the attribute this condition argument is specified for as {@code String}.
	 */
	public ACAttributeKey getKey();
	
}