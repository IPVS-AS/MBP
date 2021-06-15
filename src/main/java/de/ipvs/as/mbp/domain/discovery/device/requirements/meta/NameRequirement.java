package de.ipvs.as.mbp.domain.discovery.device.requirements.meta;

import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.operators.StringAttributeOperator;

/**
 * Objects of this class represent name requirements for devices.
 */
public class NameRequirement extends DeviceRequirement {
    //Type name of this requirement
    private static final String TYPE_NAME = "name";

    private StringAttributeOperator operator;
    private String match;

    /**
     * Creates a new name requirement.
     */
    public NameRequirement() {

    }

    /**
     * Returns the operator that is applied to the device name.
     *
     * @return The operator
     */
    public StringAttributeOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator that is applied to the device name.
     *
     * @param operator The operator to set
     * @return The name requirement
     */
    public NameRequirement setOperator(StringAttributeOperator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Returns the match string that is applied to the device name.
     *
     * @return The match
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the match string that is applied to the device name.
     *
     * @param match The match to set
     * @return The name requirement
     */
    public NameRequirement setMatch(String match) {
        this.match = match;
        return this;
    }

    /**
     * Returns the name of the requirement.
     *
     * @return The name
     */
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}
