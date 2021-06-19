package de.ipvs.as.mbp.domain.discovery.device.requirements.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.operators.BooleanOperator;
import de.ipvs.as.mbp.domain.discovery.operators.StringOperator;

/**
 * Objects of this class represent description requirements for devices.
 */
@JsonIgnoreProperties
public class DescriptionRequirement extends DeviceRequirement {
    //Type name of this requirement
    private static final String TYPE_NAME = "description";

    private BooleanOperator operator;
    private String match;

    /**
     * Creates a new description requirement.
     */
    public DescriptionRequirement() {

    }

    /**
     * Creates a new description requirement from a given boolean operator and a given match string.
     *
     * @param operator To operator to use
     * @param match    The match string to use
     */
    @JsonCreator
    public DescriptionRequirement(@JsonProperty("operator") BooleanOperator operator, @JsonProperty("match") String match) {
        setOperator(operator);
        setMatch(match);
    }

    /**
     * Returns the operator that is applied to the device description.
     *
     * @return The operator
     */
    public BooleanOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator that is applied to the device description.
     *
     * @param operator The operator to set
     * @return The description requirement
     */
    public DescriptionRequirement setOperator(BooleanOperator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Returns the match string that is applied to the device description.
     *
     * @return The match
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the match string that is applied to the device description.
     *
     * @param match The match to set
     * @return The description requirement
     */
    public DescriptionRequirement setMatch(String match) {
        this.match = match;
        return this;
    }

    /**
     * Returns the name of the requirement.
     *
     * @return The name
     */
    @JsonProperty("type")
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}
