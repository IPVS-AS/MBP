package org.citopt.connde.domain.access_control;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserialized for generic {@link ACAttribute}.
 * 
 * @param <T>
 * @author Jakob Benz
 */
public class ACAttributeDeserializer<T extends Comparable<T>> extends StdDeserializer<ACAttribute<T>> {

	protected ACAttributeDeserializer(Class<?> vc) {
		super(vc);
	}

	private static final long serialVersionUID = 5940876158156824767L;
	
	private ObjectMapper om = new ObjectMapper();


	@Override
	public ACAttribute<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JsonNode jsonnode = om.readTree(p);
		ACDataType dataType = ACDataType.valueOf(jsonnode.get("type").asText());
		String key = jsonnode.get("key").asText();
		T value = (T) om.treeToValue(jsonnode.get("value"), dataType.getClazz());
		return new ACAttribute<T>(dataType, key, value);
//		
////		JavaType type = om.getTypeFactory().constructParametricType(ACAttribute.class, handledType());
//		JavaType type = om.getTypeFactory().constructParametricType(ACAttribute.class, dataType.getClazz());
//		return om.readValue(p, type);
	}

}
