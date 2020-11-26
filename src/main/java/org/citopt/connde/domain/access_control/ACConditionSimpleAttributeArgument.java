package org.citopt.connde.domain.access_control;

import org.citopt.connde.domain.access_control.jquerybuilder.JQBRule;

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
	 * The {@link ACAttributeKey} of the attribute this attribute argument refers to.
	 */
	private ACAttributeKey key;
	
	// - - -

	/**
	 * No-args constructor.
	 */
	public ACConditionSimpleAttributeArgument() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param key the {@link ACAttributeKey} of the attribute this attribute argument refers to.
	 */
	public ACConditionSimpleAttributeArgument(ACAttributeKey key) {
		this.entityType = key.getEntityType();
		this.key = key;
	}
	
	/**
	 * All-args constructor.
	 * 
	 * @param entityType the {@link ACEntityType} of the entity this attribute argument refers to.
	 * @param key the {@link ACAttributeKey} of the attribute this attribute argument refers to.
	 */
	public ACConditionSimpleAttributeArgument(ACEntityType entityType, ACAttributeKey key) {
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
	public ACAttributeKey getKey() {
		return key;
	}
	
	public ACConditionSimpleAttributeArgument<T> setKey(ACAttributeKey key) {
		this.key = key;
		return this;
	}
	
	// - - -
	
	@Override
	public String toHumanReadableString() {
		return key.getId();
	}
	
	// - - -
	
	public static <T extends Comparable<T>> ACConditionSimpleAttributeArgument<T> basedOn(JQBRule rule) {
		return new ACConditionSimpleAttributeArgument<>(ACAttributeKey.forId(rule.getId()));
	}
	
}
