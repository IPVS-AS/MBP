package org.citopt.connde.domain.access_control;

/**
 * A simple attribute argument intended to use with {@link ACSimpleCondition}.
 * The value of the argument will be computed by retrieving the {@link ACAttribute}.
 * with the specified key of the entity according to the specified {@link ACEntityType}.
 * 
 * @param <T> the data type of this simple condition argument.
 * @author Jakob Benz
 */
public class ACConditionSimpleAttributeArgument<T extends Comparable<T>> implements IACConditionAttributeArgument<T> {
	
	/**
	 * The {@link ACEntityType} of the entity this attribute argument refers to.
	 */
	private ACEntityType entityType;
	
	/**
	 * The key of the attribute thiss attribute argument refers to.
	 */
	private String key;
	
	// - - -

	/**
	 * No-args constructor.
	 */
	public ACConditionSimpleAttributeArgument() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param entityType the {@link ACEntityType} of the entity this attribute argument refers to.
	 * @param key the key of the attribute this attribute argument refers to.
	 */
	public ACConditionSimpleAttributeArgument(ACEntityType entityType, String key) {
		this.entityType = entityType;
		this.key = key;
	}
	
	// - - -

	@Override
	public ACEntityType getEntityType() {
		return entityType;
	}
	
	public ACConditionSimpleAttributeArgument<T> setEntityType(ACEntityType entityType) {
		this.entityType = entityType;
		return this;
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
	public ACConditionSimpleAttributeArgument<T> setKey(String key) {
		this.key = key;
		return this;
	}
	
}
