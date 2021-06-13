package de.ipvs.as.mbp.domain.discovery.operators;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Enumeration of operators that can be applied to string attributes.
 */
public enum StringAttributeOperator {
    EQUALS, CONTAINS, BEGINS_WITH, ENDS_WITH;

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
            if (name.equalsIgnoreCase(operator.toString())) {
                //Matching operator found
                return operator;
            }
        }

        //No matching string attribute operator was found
        return null;
    }
}
