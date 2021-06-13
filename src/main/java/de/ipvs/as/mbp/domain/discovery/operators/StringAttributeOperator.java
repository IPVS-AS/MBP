package de.ipvs.as.mbp.domain.discovery.operators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of operators that can be applied to string attributes.
 */
public enum StringAttributeOperator {
    EQUALS("equals"), CONTAINS("contains"), BEGINS_WITH("begins_with"), ENDS_WITH("ends_with");

    //Externally visible name of the operator
    private String name;

    /**
     * Creates a new string attribute operator with a given name.
     *
     * @param name The desired name of the operator
     */
    StringAttributeOperator(String name) {
        setName(name);
    }

    /**
     * Returns the name of the string attribute operator.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the string attribute operator.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Serializes a string attribute operator by returning its name.
     *
     * @return The name of the operator
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the string attribute operator that corresponds to a given name. This method is called when
     * a provided operator name needs to be mapped to the actual operator object.
     *
     * @param name The name of the string attribute operator
     * @return The corresponding string attribute operator
     */
    @JsonCreator
    public static StringAttributeOperator create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available string attribute operator against the provided name
        for (StringAttributeOperator operator : values()) {
            if (name.equalsIgnoreCase(operator.getName())) {
                //Matching operator found
                return operator;
            }
        }

        //No matching string attribute operator was found
        return null;
    }
}
