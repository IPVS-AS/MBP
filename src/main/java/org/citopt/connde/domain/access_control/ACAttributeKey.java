package org.citopt.connde.domain.access_control;

import org.citopt.connde.domain.user.User;

/**
 * Enumeration for some standard attribute keys.
 * 
 * @author Jakob Benz
 */
public enum ACAttributeKey {
	
	/**
	 * The id of an entity {@link User owner}.
	 */
	ENTITY_OWNER_ID(User.class, "entity-owner-id", "id"),
	
	/**
	 * The username of an entity {@link User owner}. 
	 */
	ENTITY_OWNER_USERNAME(User.class, "entity-owner-username", "username"),
	
	/**
	 * <b>NOT INTENDED FOR USE!</b>
	 * <br>
	 * Only used for the default value in {@link ACAttributeValue#key()}.
	 */
	NULL(Void.class, "", "");
	
	// - - -
	
	private Class<?> targetType;
	private String key;
	private String valueLookupPath;
	
	private ACAttributeKey(Class<?> targetType, String key, String valueLookupPath) {
		this.targetType = targetType;
		this.key = key;
		this.valueLookupPath = valueLookupPath;
	}
	
	public Class<?> getTargetType() {
		return targetType;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getValueLookupPath() {
		return valueLookupPath;
	}

}
