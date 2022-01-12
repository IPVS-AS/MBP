package de.ipvs.as.mbp.domain.access_control;

/**
 * Enumeration for some standard attribute keys.
 * 
 * @author Jakob Benz
 */
public enum ACAttributeKey {
	
	/**
	 * The username of the requesting entity.
	 */
	REQUESTING_ENTITY_USERNAME("requesting-entity-username", "The username of the requesting entity (user).", String.class, "username", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The first name of the requesting entity.
	 */
	REQUESTING_ENTITY_FIRSTNAME("requesting-entity-firstname", "The first name of the requesting entity (user).", String.class, "firstName", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The last name of the requesting entity.
	 */
	REQUESTING_ENTITY_LASTNAME("requesting-entity-lastname", "The last name of the requesting entity (user).", String.class, "lastName", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * The last name of the requesting entity.
	 */
	REQUESTING_ENTITY_IS_ADMIN("requesting-entity-isAdmin", "The admin status of the equesting entity (user).", String.class, "isAdmin", ACEntityType.REQUESTING_ENTITY),
	
	/**
	 * <b>NOT INTENDED FOR USE!</b>
	 * <br>
	 * Only used for the default value in {@link ACAttributeValue#key()}.
	 */
	NULL("", "", Void.class, "", null);
	
	// - - -
	
	private final String id;
	private final String description;
	private final Class<?> type;
	private final String valueLookupPath;
	private final ACEntityType entityType;
	
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
