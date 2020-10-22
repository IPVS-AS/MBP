package org.citopt.connde.domain.access_control;

/**
 * Enumeration for some standard attribute keys.
 * 
 * @author Jakob Benz
 */
public enum ACAttributeKey {
	
//	/**
//	 * The id of the requesting entity.
//	 */
//	REQUESTING_ENTITY_ID("requesting-entity-id", "The id of the requesting entity (user).", String.class, "id", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The username of the requesting entity.
	 */
	REQUESTING_ENTITY_USERNAME("username", "The username of the requesting entity (user).", String.class, "username", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The first name of the requesting entity.
	 */
	REQUESTING_ENTITY_FIRSTNAME("firstname", "The first name of the requesting entity (user).", String.class, "firstName", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The last name of the requesting entity.
	 */
	REQUESTING_ENTITY_LASTNAME("lastname", "The last name of the requesting entity (user).", String.class, "lastName", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The last name of the requesting entity.
	 */
	REQUESTING_ENTITY_IS_ADMIN("isAdmin", "The admin status of the equesting entity (user).", String.class, "isAdmin", ACEntityType.REQUESTING_ENTITY),
	
//	/**
//	 * The id of an entity {@link User owner}.
//	 */
//	ENTITY_OWNER_ID("entity-owner-id", "The id of the owning entity.", String.class, "owner.id", ACEntityType.REQUESTED_ENTITY),
//	
//	/**
//	 * The username of an entity {@link User owner}. 
//	 */
//	ENTITY_OWNER_USERNAME("entity-owner-username", "The username of the owning entity (user).", String.class, "owner.username", ACEntityType.REQUESTED_ENTITY),
	
	/**
	 * <b>NOT INTENDED FOR USE!</b>
	 * <br>
	 * Only used for the default value in {@link ACAttributeValue#key()}.
	 */
	NULL("", "", Void.class, "", null);
	
	// - - -
	
	private String id;
	private String description;
	private Class<?> type;
	private String valueLookupPath;
	private ACEntityType entityType;
	
	private ACAttributeKey(String id, String description, Class<?> type, String valueLookupPath, ACEntityType entityType) {
		this.id = id;
		this.description = description;
		this.type = type;
		this.valueLookupPath = valueLookupPath;
		this.entityType = entityType;
	}
	
	public String getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
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
