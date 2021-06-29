package de.ipvs.as.mbp.domain.discovery.description;

/**
 * Objects of this class describe capabilities of devices within {@link DeviceDescription}s. Thereby, each capability
 * consists out of a name, a value of type {@link String}, {@link Double} or {@link Boolean} and an optional flag
 * indicating whether the capability behaves cumulative.
 */
public class DeviceDescriptionCapability extends DeviceDescriptionIdentifiers {
    private String name;
    private Object value;
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
