package de.ipvs.as.mbp.domain.operator.parameters;

/**
 * Objects of this class represent deployment parameters that can be added to operators. If a sensor is
 * supposed to be deployed that uses such an operator, the values for the parameters need to be provided
 * as parameter instances by the user.
 */
public class Parameter {
    private String name;
    private ParameterType type;
    private String unit;
    private boolean mandatory;

    /**
     * Creates a new empty deployment parameter that may be added to an operator.
     */
    public Parameter() {

    }

    /**
     * Creates a new deployment parameter with a given name, parameter type and unit. In addition, it can
     * be specified whether the parameter is mandatory for the deployment.
     *
     * @param name      The name of the parameter
     * @param type      The type of the parameter
     * @param unit      The unit of the parameter
     * @param mandatory True, if the parameter is mandatory; false otherwise
     */
    public Parameter(String name, ParameterType type, String unit, boolean mandatory) {
        this.name = name;
        this.type = type;
        this.unit = unit;
        this.mandatory = mandatory;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the parameter.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the parameter.
     *
     * @return The type
     */
    public ParameterType getType() {
        return type;
    }

    /**
     * Sets the type of the parameter.
     *
     * @param type The type to set
     */
    public void setType(ParameterType type) {
        this.type = type;
    }

    /**
     * Returns the unit of the parameter.
     * @return The unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Sets the unit of the parameter.
     * @param unit The unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Returns whether the parameter is mandatory.
     *
     * @return True, if the parameter is mandatory; false otherwise
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * Determines whether the parameter is mandatory.
     *
     * @param mandatory True, if the parameter is mandatory; false otherwise
     */
    public void setMandatory(boolean mandatory) {
        this.mandatory = mandatory;
    }

    /**
     * Verifies if a given parameter instance is a valid instance for this parameter. In order to
     * be valid, the instance name and type must correspond to the name and type of the parameter.
     *
     * @param instance The parameter instance to check
     * @return True, if the parameter instance is a valid instance; false otherwise
     */
    public boolean isInstanceValid(ParameterInstance instance) {
        //Check name
        if (!instance.getName().equals(this.name)) {
            return false;
        }

        //Check data type
        Object value = instance.getValue();
        switch (this.type) {
            case BOOLEAN:
                //Value must be of type boolean
                if (!(value instanceof Boolean)) {
                    return false;
                }
                break;
            case NUMBER:
                //Value must be either of type double or integer
                if (!((value instanceof Double) || (value instanceof Integer))) {
                    return false;
                }
                break;
            case TEXT:
                //Value must be of type string
                if (!(value instanceof String)) {
                    return false;
                }
                String stringValue = (String) value;
                //Value most not be empty except if the parameter is not mandatory
                if (this.mandatory && stringValue.isEmpty()) {
                    return false;
                }
                break;
        }
        return true;
    }
}
