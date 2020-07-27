package org.citopt.connde.domain.access_control;

import javax.annotation.Nonnull;

/**
 * An attribute wraps a piece of information with a type as a key-value pair.
 * Within the MBP access-control framework it is used to store contextual information
 * of requesting entities such as users, and requested entities, such as sensors.
 * 
 * @param <T> the data type of this attribute.
 * @author Jakob Benz
 */
public class ACAttribute<T extends Comparable<T>> {
	
	// User
	public static final String KEY_USER_ID = "user.id";
	public static final String KEY_USER_USERNAME = "user.username";
	public static final String KEY_USER_FIRST_NAME = "user.firstName";
	public static final String KEY_USER_LAST_NAME = "user.lastName";
	
	// User entity
	public static final String KEY_USER_ENTITY_ID = "userEntity.id";
	public static final String KEY_USER_ENTITY_ENVIRONMENT_MODEL = "userEntity.environmentModel";
	public static final String KEY_USER_ENTITY_OWNER = "userEntity.owner";
	public static final String KEY_USER_ENTITY_CREATED = "userEntity.created";
	public static final String KEY_USER_ENTITY_UPDATEd = "userEntity.lastModified";
	
	/**
	 * The {@link ACDataType type} of this attribute.
	 */
	@Nonnull
	private ACDataType type;
	
	/**
	 * The key of the attribute. 
	 */
	@Nonnull
	private String key;
	
	/**
	 * The value of the attribute.. 
	 */
	@Nonnull
	private T value;
	
	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param type the {@link ACDataType type} of this attribute.
	 * @param key the key of the attribute.
	 * @param value the value of the attribute.
	 */
	public ACAttribute(ACDataType type, String key, T value) {
		this.type = type;
		this.key = key;
		this.value = value;
	}

	// - - -
	
	public ACDataType getType() {
		return type;
	}

	public ACAttribute<T> setType(ACDataType type) {
		this.type = type;
		return this;
	}
	
	public String getKey() {
		return key;
	}

	public  ACAttribute<T> setKey(String key) {
		this.key = key;
		return this;
	}

	public T getValue() {
		return value;
	}

	public  ACAttribute<T> setValue(T value) {
		this.value = value;
		return this;
	}
	
}
