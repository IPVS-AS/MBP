package de.ipvs.as.mbp.domain.discovery.description;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Objects of this class describe capabilities of devices within {@link DeviceDescription}s. Thereby, each capability
 * consists out of a name, a value of type {@link String}, {@link Double} or {@link Boolean} and an optional flag
 * indicating whether the capability behaves cumulative.
 */
public class DeviceDescriptionCapability {

    /**
     * Exception that is thrown in case a capability value is of a type that was not expected by the caller that
     * tried to access it.
     */
    public static class TypeMismatchException extends RuntimeException {
        /**
         * Creates a new {@link TypeMismatchException} from a given error message.
         *
         * @param message The error message to use
         */
        public TypeMismatchException(String message) {
            super(message);
        }
    }

    //Name of the capability
    private String name;

    //Value of the capability
    private Object value;

    //Whether the capability behaves cumulative
    private boolean cumulative = false;

    /**
     * Creates a new, empty capability description.
     */
    public DeviceDescriptionCapability() {

    }

    /**
     * Creates a new capability description from a given name and a given value.
     *
     * @param name  The name of the capability to use
     * @param value The value of the capability to use.
     */
    public DeviceDescriptionCapability(String name, Object value) {
        //Set fields
        setName(name);
        setValue(value);
    }

    /**
     * Returns the name of the capability.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the capability.
     *
     * @param name The name to set
     * @return The capability description
     */
    public DeviceDescriptionCapability setName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("The name of the capability must not be null or empty.");
        }

        this.name = name;
        return this;
    }

    /**
     * Checks and returns whether the value of this capability is of type string or null.
     *
     * @return True, if the capability value is of type string or null; false otherwise
     */
    public boolean isString() {
        //Check for null and type
        return (this.value == null) || (this.value instanceof String);
    }

    /**
     * Checks and returns whether the value of this capability is of type number.
     *
     * @return True, if the capability value is of type number; false otherwise
     */
    public boolean isNumber() {
        //Check for null and type
        return (this.value != null) && (this.value instanceof Double);
    }

    /**
     * Checks and returns whether the value of this capability is of type boolean.
     *
     * @return True, if the capability value is of type boolean; false otherwise
     */
    public boolean isBoolean() {
        //Check for null and type
        return (this.value != null) && (this.value instanceof Boolean);
    }

    /**
     * Returns the value of this capability as string. If the capability value is not of type string, an exception
     * is thrown.
     *
     * @return The capability value as string
     */
    @JsonIgnore
    public String getValueAsString() {
        //Type check
        if (!isString()) throw new TypeMismatchException("The capability value is not of type string.");

        //Parse the value as string
        return this.value.toString();
    }

    /**
     * Returns the value of this capability as double. If the capability value is not of type number, an exception
     * is thrown.
     *
     * @return The capability value as double
     */
    @JsonIgnore
    public double getValueAsDouble() {
        //Type check
        if (!isNumber()) throw new TypeMismatchException("The capability value is not of type double.");

        //Parse the value as string
        return (Double) this.value;
    }

    /**
     * Returns the value of this capability as boolean. If the capability value is not of type boolean, an exception
     * is thrown.
     *
     * @return The capability value as boolean
     */
    @JsonIgnore
    public boolean getValueAsBoolean() {
        //Type check
        if (!isBoolean()) throw new TypeMismatchException("The capability value is not of type boolean.");

        //Parse the value as string
        return (Boolean) this.value;
    }

    /**
     * Returns the value of the capability.
     *
     * @return The value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the capability which may be either of type {@link String}, {@link Double} or {@link Boolean}.
     *
     * @param value The value to set
     * @return The capability description
     */
    public DeviceDescriptionCapability setValue(Object value) {
        //Check if value type is valid
        checkValueType(value);

        this.value = value;
        return this;
    }

    /**
     * Returns whether the capability behaves cumulative.
     *
     * @return True, if the capability behaves cumulative; false otherwise
     */
    public boolean isCumulative() {
        return cumulative;
    }

    /**
     * Sets whether the capability behaves cumulative.
     *
     * @param cumulative True, if the capability behaves cumulative; falser otherwise
     * @return The capability description
     */
    public DeviceDescriptionCapability setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
        return this;
    }

    /**
     * Checks whether a given value is of one of the accepted data types ({@link String}, {@link Double} or
     * {@link Boolean}). If this is not the case, an exception is thrown.
     *
     * @param value The value to check
     */
    private void checkValueType(Object value) {
        //Check against valid types
        if (!((value instanceof String) || (value instanceof Double) || (value instanceof Boolean))) {
            throw new IllegalArgumentException("Value is of an illegal type.");
        }
    }
}
