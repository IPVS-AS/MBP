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
public class ACAttribute {
	
	/**
	 * The {@link ACDataType type} of this attribute.
	 */
	@Nonnull
	private ACDataType type;
	
	/**
	 * The {@link ACAttributeKey} of the attribute. 
	 */
	@Nonnull
	private ACAttributeKey key;
	
	/**
	 * The value of the attribute.. 
	 */
	@Nonnull
	private Object value;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAttribute() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param type the {@link ACDataType type} of this attribute.
	 * @param key the {@link ACAttributeKey} of the attribute.
	 * @param value the value of the attribute.
	 */
	public ACAttribute(ACDataType type, ACAttributeKey key, Object value) {
		this.type = type;
		this.key = key;
		this.value = value;
	}

	// - - -
	
	public ACDataType getType() {
		return type;
	}

	public ACAttribute setType(ACDataType type) {
		this.type = type;
		return this;
	}
	
	public ACAttributeKey getKey() {
		return key;
	}

	public  ACAttribute setKey(ACAttributeKey key) {
		this.key = key;
		return this;
	}

	public Object getValue() {
		return value;
	}

	public  ACAttribute setValue(Object value) {
		this.value = value;
		return this;
	}
	
//	public static void main(String[] args) throws JsonProcessingException {
//		ACAttribute<String> a = new ACAttribute<String>(ACDataType.ALPHABETIC, "a1", "av1");
//		JsonNode jsonNode = new ObjectMapper().valueToTree(a);
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode));
//		ACAttribute<? extends Comparable<?>> aa = new ObjectMapper().treeToValue(jsonNode, ACAttribute.class);
//		System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(aa));
//	}
	
}
