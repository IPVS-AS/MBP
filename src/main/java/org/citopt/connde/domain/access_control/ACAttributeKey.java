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
	REQUESTING_ENTITY_ID("requesting-entity-id", String.class, "id", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The username of the requesting entity.
	 */
	REQUESTING_ENTITY_USERNAME("requesting-entity-username", String.class, "username", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The first name of the requesting entity.
	 */
	REQUESTING_ENTITY_FIRSTNAME("requesting-entity-firstname", String.class, "firstName", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The last name of the requesting entity.
	 */
	REQUESTING_ENTITY_LASTNAME("requesting-entity-lastname", String.class, "lastName", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The id of an entity {@link User owner}.
	 */
	ENTITY_OWNER_ID("entity-owner-id", String.class, "owner.id", ACEntityType.REQUESTED_ENTITY),
	
	/**
	 * The username of an entity {@link User owner}. 
	 */
	ENTITY_OWNER_USERNAME("entity-owner-username", String.class, "owner.username", ACEntityType.REQUESTED_ENTITY),
	
	/**
	 * <b>NOT INTENDED FOR USE!</b>
	 * <br>
	 * Only used for the default value in {@link ACAttributeValue#key()}.
	 */
	NULL("", Void.class, "", null);
	
	// - - -
	
	private String id;
	private Class<?> type;
	private String valueLookupPath;
	private ACEntityType entityType;
	
	private ACAttributeKey(String id, Class<?> type, String valueLookupPath, ACEntityType entityType) {
		this.id = id;
		this.type = type;
		this.valueLookupPath = valueLookupPath;
		this.entityType = entityType;
	}
	
	public String getId() {
		return id;
	}
	
	public Class<?> getType() {
		return type;
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
