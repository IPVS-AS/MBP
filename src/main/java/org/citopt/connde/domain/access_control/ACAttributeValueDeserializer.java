package org.citopt.connde.domain.access_control;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

/**
 * Custom deserialized for generic attribute values.
 * 
 * @param <T>
 * @author Jakob Benz
 */
public class ACAttributeValueDeserializer<T extends Comparable<T>> extends StdDeserializer<T> {

	private static final long serialVersionUID = 5940876158156824767L;
	
	private ObjectMapper om = new ObjectMapper();

	protected ACAttributeValueDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public T deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
		JavaType type = om.getTypeFactory().constructParametricType(ACAttribute.class, handledType());
		return om.readValue(p, type);
	}

}
