package de.ipvs.as.mbp.domain.discovery.device.operators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.function.BiFunction;

/**
 * Enumeration of operators that can be applied to string attributes. Each operator consists out of a externally
 * visible name and a application function that allows to apply the operator to given target and match strings and
 * retrieve the corresponding boolean results.
 */
public enum StringOperator implements DiscoveryTemplateOperator {
    EQUALS("equals", String::equalsIgnoreCase),
    CONTAINS("contains", (target, match) -> target.toLowerCase().contains(match.toLowerCase())),
    BEGINS_WITH("begins_with", (target, match) -> target.toLowerCase().startsWith(match.toLowerCase())),
    ENDS_WITH("ends_with", (target, match) -> target.toLowerCase().endsWith(match.toLowerCase())),
    NOT_EQUALS("not_equals", (target, match) -> !target.equalsIgnoreCase(match));

    //Externally visible name of the operator
    private String name;
    //Function (target, match, result) that applies the operator to given target and match strings
    private BiFunction<String, String, Boolean> applicationFunction;

    /**
     * Creates a new string operator from a given name and a given application function that allows to apply
     * the operator to given target and match strings.
     *
     * @param name                The desired name of the operator
     * @param applicationFunction The function that applies the operator to given target and match strings
     */
    StringOperator(String name, BiFunction<String, String, Boolean> applicationFunction) {
        setName(name);
        setApplicationFunction(applicationFunction);
    }

    /**
     * Applies the string operator to given target and match strings and returns the boolean result.
     *
     * @param target The target string to apply the operator to
     * @param match  The match string to use
     * @return True, if a match between the target and the match string is found; false otherwise
     */
    public boolean apply(String target, String match) {
        //Null check
        if ((target == null) || (match == null)) {
            return false;
        }

        //Apply the application function
        return applicationFunction.apply(target, match);
    }

    /**
     * Sets the name of the string operator.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("The name must not be null or empty.");
        }

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
     * Returns the function that applies the operator to given target and match strings and returns the result as
     * boolean.
     *
     * @return The application function
     */
    public BiFunction<String, String, Boolean> getApplicationFunction() {
        return applicationFunction;
    }

    /**
     * Sets the function that applies the operator to given target and match strings and returns the result as boolean.
     *
     * @param applicationFunction The application function to set
     */
    public void setApplicationFunction(BiFunction<String, String, Boolean> applicationFunction) {
        //Null check
        if (applicationFunction == null) {
            throw new IllegalArgumentException("The application function must not be null.");
        }

        this.applicationFunction = applicationFunction;
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
