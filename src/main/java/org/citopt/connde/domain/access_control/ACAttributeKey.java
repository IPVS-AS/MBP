package org.citopt.connde.domain.access_control;

import org.citopt.connde.domain.user.User;

/**
 * Enumeration for some standard attribute keys.
 * 
 * @author Jakob Benz
 */
public enum ACAttributeKey {
	
	/**
	 * The id of the requesting entity.
	 */
	REQUESTING_ENTITY_ID(User.class, "requesting-entity-id", "id", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The username of the requesting entity.
	 */
	REQUESTING_ENTITY_USERNAME(User.class, "requesting-entity-username", "username", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The id of an entity {@link User owner}.
	 */
	ENTITY_OWNER_ID(User.class, "entity-owner-id", "owner.id", ACEntityType.REQUESTED_ENTITY),
	
	/**
	 * The username of an entity {@link User owner}. 
	 */
	ENTITY_OWNER_USERNAME(User.class, "entity-owner-username", "owner.username", ACEntityType.REQUESTED_ENTITY),
	
	/**
	 * <b>NOT INTENDED FOR USE!</b>
	 * <br>
	 * Only used for the default value in {@link ACAttributeValue#key()}.
	 */
	NULL(Void.class, "", "", null);
	
	// - - -
	
	private Class<?> targetType;
	private String id;
	private String valueLookupPath;
	private ACEntityType entityType;
	
	private ACAttributeKey(Class<?> targetType, String id, String valueLookupPath, ACEntityType entityType) {
		this.targetType = targetType;
		this.id = id;
		this.valueLookupPath = valueLookupPath;
		this.entityType = entityType;
	}
	
	public Class<?> getTargetType() {
		return targetType;
	}
	
	public String getId() {
		return id;
	}
	
	public String getValueLookupPath() {
		return valueLookupPath;
	}
	
	public ACEntityType getEntityType() {
		return entityType;
	}
	
	// - - -
	
	public static ACAttributeKey forId(String id) {
		for (ACAttributeKey key : ACAttributeKey.values()) {
			if (key.id.equals(id)) {
				return key;
			}
		}
		return NULL;
	}

}
