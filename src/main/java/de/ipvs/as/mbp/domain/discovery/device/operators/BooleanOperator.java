package de.ipvs.as.mbp.domain.discovery.device.operators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of binary operators that can be applied to boolean attributes.
 */
public enum BooleanOperator implements DiscoveryTemplateOperator {
    AND("and"), OR("or");

    //Externally visible name of the operator
    private String name;

    /**
     * Creates a new boolean operator with a given name.
     *
     * @param name The desired name of the operator
     */
    BooleanOperator(String name) {
        setName(name);
    }

    /**
     * Sets the name of the boolean operator.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if((name == null) || name.isEmpty()){
            throw new IllegalArgumentException("The name must not be null or empty.");
        }

        this.name = name;
    }

    /**
     * Serializes a boolean operator by returning its name.
     *
     * @return The name of the operator
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the boolean operator that corresponds to a given name. This method is called when
     * a provided operator name needs to be mapped to the actual operator object.
     *
     * @param name The name of the boolean operator
     * @return The corresponding boolean operator or null if not found
     */
    @JsonCreator
    public static BooleanOperator create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available operator against the provided name
        for (BooleanOperator operator : values()) {
            if (name.equalsIgnoreCase(operator.value())) {
                //Matching operator found
                return operator;
            }
        }

        //No matching operator was found
        return null;
    }
}
