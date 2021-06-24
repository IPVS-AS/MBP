package de.ipvs.as.mbp.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Collection of JSON-related utility functions, including a pre-configured Jackson-Mapper for serializing
 * and de-serializing domain objects.
 */
public class Json {
    //Create pre-configured JSON mapper for general usage
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
}
