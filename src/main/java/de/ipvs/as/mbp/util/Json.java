package de.ipvs.as.mbp.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;

/**
 * Collection of JSON-related utility functions, including a pre-configured Jackson-Mapper for serializing
 * and de-serializing domain objects.
 */
public class Json {
    //Create pre-configured JSON mapper for general usage
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Transforms a given {@link Object} to a JSON string. If the transformation fails, the JSON string
     * of an empty JSON object is returned instead.
     *
     * @param object The object to transform
     * @return The resulting JSON string
     */
    public static String of(Object object) {
        //Sanity check
        if (object == null) {
            throw new IllegalArgumentException("Object must not be null.");
        }

        try {
            //Transform object to JSON string
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            //Failed
            System.err.printf("Failed to convert %s to JSON string: %s%n", object.getClass().getName(), e.getMessage());
            return new JSONObject().toString();
        }
    }

    /**
     * Creates an object of a certain type, given as {@link TypeReference}, from a given JSON string.
     * If the transformation fails, null will be returned.
     *
     * @param jsonString   The JSON string to transform
     * @param valueTypeRef References the the type of the target object that is supposed to be created
     * @param <T>          The referenced type of the target object
     * @return The resulting object of type {@link T} or null, if the transformation failed
     */
    public static <T> T toObject(String jsonString, TypeReference<T> valueTypeRef) {
        try {
            //Transform JSON string to object of given type
            return Json.MAPPER.readValue(jsonString, valueTypeRef);
        } catch (JsonProcessingException e) {
            //Failed
            System.err.printf("Failed to create %s from JSON string: %s%n", valueTypeRef.getType().getClass().getName(),
                    e.getMessage());
            return null;
        }
    }
}
