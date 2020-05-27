package org.citopt.connde.service.env_model.events.types;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class EnvironmentModelEvent {
    //Object mapper for converting events to JSON objects
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Returns the name of the event, allowing to identify and recognize its type.
     *
     * @return The name of the event
     */
    public abstract String getName();

    /**
     * Transforms the event to a JSON string and returns it.
     *
     * @return The generated JSON string
     */
    public String toString() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "Failed to convert event to JSON";
        }
    }
}
