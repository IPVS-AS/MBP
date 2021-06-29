package de.ipvs.as.mbp.domain.discovery.operators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of operators that can be applied to string attributes.
 */
public enum StringOperator implements DiscoveryTemplateOperator {
    EQUALS("equals"), CONTAINS("contains"), BEGINS_WITH("begins_with"), ENDS_WITH("ends_with"),
    NOT_EQUALS("not_equals");

    //Externally visible name of the operator
    private String name;

    /**
     * Creates a new string operator with a given name.
     *
     * @param name The desired name of the operator
     */
    StringOperator(String name) {
        setName(name);
    }

    /**
     * Sets the name of the string operator.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Serializes a string operator by returning its name.
     *
     * @return The name of the operator
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the string operator that corresponds to a given name. This method is called when
     * a provided operator name needs to be mapped to the actual operator object.
     *
     * @param name The name of the string operator
     * @return The corresponding string operator or null if not found
     */
    @JsonCreator
    public static StringOperator create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available operator against the provided name
        for (StringOperator operator : values()) {
            if (name.equalsIgnoreCase(operator.value())) {
                //Matching operator found
                return operator;
            }
        }

        //No matching operator was found
        return null;
    }
}
