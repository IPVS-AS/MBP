package de.ipvs.as.mbp.domain.discovery.device.requirements.meta;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.operators.StringOperator;
import de.ipvs.as.mbp.error.EntityValidationException;

/**
 * Objects of this class represent name requirements for devices.
 */
@JsonIgnoreProperties
public class NameRequirement extends DeviceRequirement {
    //Type name of this requirement
    private static final String TYPE_NAME = "name";

    private StringOperator operator;
    private String match;

    /**
     * Creates a new name requirement.
     */
    public NameRequirement() {

    }

    /**
     * Creates a new name requirement from a given string operator and a given match string.
     *
     * @param operator To operator to use
     * @param match    The match string to use
     */
    @JsonCreator
    public NameRequirement(@JsonProperty("operator") StringOperator operator, @JsonProperty("match") String match) {
        setOperator(operator);
        setMatch(match);
    }

    /**
     * Returns the operator that is applied to the device name.
     *
     * @return The operator
     */
    public StringOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator that is applied to the device name.
     *
     * @param operator The operator to set
     * @return The name requirement
     */
    public NameRequirement setOperator(StringOperator operator) {
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
    @JsonProperty("type")
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Validates the device requirement by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    @Override
    public void validate(EntityValidationException exception, String fieldPrefix) {
        //Check operator
        if (operator == null) {
            exception.addInvalidField(fieldPrefix + ".operator", "An operator must be selected.");
        }

        //Check match
        if ((match == null) || (match.isEmpty())) {
            exception.addInvalidField(fieldPrefix + ".match", "The match must not be empty.");
        }
    }


}
