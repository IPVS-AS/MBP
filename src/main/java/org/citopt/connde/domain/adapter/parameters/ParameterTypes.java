package org.citopt.connde.domain.adapter.parameters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of types for deployment parameters that are offered to the user.
 */
public enum ParameterTypes {
    TEXT("Text"), NUMBER("Number"), BOOLEAN("Switch");

    //Name of the parameter type that is displayed to the user
    private String name;

    /**
     * Creates a new parameter type with a specific name.
     *
     * @param name The name of the parameter type
     */
    ParameterTypes(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the parameter type as it may presented to the user.
     *
     * @return The name of the parameter type
     */
    @Override
    public String toString() {
        return this.name;
    }

    /**
     * Returns the parameter type that corresponds to a certain type name. This method is called when
     * the client uses the name of a parameter in its request that needs to be mapped to an actual
     * parameter type object.
     *
     * @param name The name of the requested parameter type
     * @return The corresponding parameter type
     */
    @JsonCreator
    public static ParameterTypes create(String name) {
        //Check for invalid name
        if (name == null) {
            return null;
        }

        //Compare every available parameter type to the provided name
        for (ParameterTypes type : values()) {
            if (name.equals(type.toString())) {
                //Parameter found
                return type;
            }
        }

        //No matching parameter type was found
        return null;
    }

    /**
     * Returns the name of the parameter type as it may presented to the user. This method is called
     * when a representation of the parameter type needs to be returned to the client.
     *
     * @return The name of the parameter type
     */
    @JsonValue
    public String toValue() {
        return this.toString();
    }
}
