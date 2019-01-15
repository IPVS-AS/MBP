package org.citopt.connde.domain.adapter.parameters;

/**
 * Objects of this class represent instantiated deployment parameters for which an actual value was provided.
 */
public class ParameterInstance {
    //Name and value of the parameter
    private String name;
    private Object value;

    /**
     * Creates a new empty instance for a parameter without name and value.
     */
    public ParameterInstance() {
    }

    /**
     * Creates a new instance for a parameter with a given name and value.
     *
     * @param name  The name of the parameter to which the instance belongs
     * @param value The parameter value
     */
    public ParameterInstance(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name of the parameter to which the instance belongs.
     *
     * @return The name of the parameter
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the parameter to which the instance belongs.
     *
     * @param name The name of the parameter
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the value of this parameter instance.
     *
     * @return The parameter value
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of this parameter instance.
     *
     * @param value The parameter value
     */
    public void setValue(Object value) {
        this.value = value;
    }
}
