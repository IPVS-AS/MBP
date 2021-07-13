package de.ipvs.as.mbp.domain.discovery.device.scoring.capability;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionCapability;
import de.ipvs.as.mbp.domain.discovery.device.operators.StringOperator;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;

import java.util.Optional;

/**
 * Objects of this class represent scoring criteria for capabilities of devices that are expressed as strings.
 */
@JsonIgnoreProperties
public class StringCapabilityScoringCriterion extends CapabilityScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "string_capability";

    //The operator use
    private StringOperator operator;

    //The match string to use
    private String match;

    //Increment/decrement of the score
    private double scoreIncrement;

    /**
     * Creates a new scoring criterion for a capability that is expressed as string.
     */
    public StringCapabilityScoringCriterion() {

    }

    /**
     * Creates a new scoring criterion for a capability that is expressed as string by using the name of the pertaining
     * capability, a string operator, a match string and a score increment/decrement.
     *
     * @param capabilityName The name of the capability to which this criterion is supposed to be applied
     * @param operator       The {@link StringOperator} to apply to the capability value
     * @param match          The match string to apply to the capability value, using the operator
     * @param scoreIncrement The score increment/decrement that is supposed to be added to the score of the device
     *                       description in case of a match.
     */
    @JsonCreator
    public StringCapabilityScoringCriterion(@JsonProperty("capabilityName") String capabilityName,
                                            @JsonProperty("operator") StringOperator operator,
                                            @JsonProperty("match") String match,
                                            @JsonProperty("scoreIncrement") double scoreIncrement) {
        //Set fields
        super(capabilityName);
        setOperator(operator);
        setMatch(match);
        setScoreIncrement(scoreIncrement);
    }

    /**
     * Returns the operator that is applied to the value of the pertaining capability.
     *
     * @return The operator
     */
    public StringOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator that is applied to the value of the pertaining capability.
     *
     * @param operator The operator to set
     * @return The string capability scoring criterion
     */
    public StringCapabilityScoringCriterion setOperator(StringOperator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Returns the match string that is applied to the value of the pertaining capability.
     *
     * @return The match string
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the match string that is applied to the value of the pertaining capability.
     *
     * @param match The match string to set
     * @return The string capability scoring criterion
     */
    public StringCapabilityScoringCriterion setMatch(String match) {
        this.match = match;
        return this;
    }

    /**
     * Returns the score increment (positive number) or decrement (negative number) that is supposed to be added
     * to the score of the device description in case of a match.
     *
     * @return The score increment
     */
    public double getScoreIncrement() {
        return scoreIncrement;
    }

    /**
     * Sets the score increment (positive number) or decrement (negative number) that is supposed to be added
     * to the score of the device description in case of a match.
     *
     * @param scoreIncrement The score increment to set
     * @return The string capability scoring criterion
     */
    public StringCapabilityScoringCriterion setScoreIncrement(double scoreIncrement) {
        this.scoreIncrement = scoreIncrement;
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
     * Validates the device scoring criterion by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    @Override
    public void validate(EntityValidationException exception, String fieldPrefix) {
        //Call validation method of super class
        super.validate(exception, fieldPrefix);

        //Check operator
        if (operator == null) {
            exception.addInvalidField(fieldPrefix + ".operator", "An operator must be selected.");
        }

        //Check match
        if ((match == null) || (match.isEmpty())) {
            exception.addInvalidField(fieldPrefix + ".match", "The match string must not be empty.");
        }

        //Check increment
        if (scoreIncrement == 0) {
            exception.addInvalidField(fieldPrefix + ".scoreIncrement", "The score increment must not be zero.");
        }
    }

    /**
     * Applies the scoring criterion to a given {@link DeviceDescription} and returns the resulting scoring increment
     * (positive number) or scoring decrement (negative number) for this description as result. In addition, a
     * reference to the {@link DeviceDescriptionScorer} that currently performs the overall score calculations is
     * provided, which may contain additional information about the collection of {@link DeviceDescription}s that are
     * currently subject to the scoring process and thus enables the calculation of relative scores.
     *
     * @param deviceDescription The device description for which the score increment of this scoring criterion is
     *                          supposed to be calculated
     * @param scorer            The {@link DeviceDescriptionScorer} that currently performs the overall score
     *                          calculations for a collection of {@link DeviceDescription}s.
     * @return The score increment/decrement that results from the application of this scoring criterion to the given
     * device description
     */
    @Override
    public double getScoreIncrement(DeviceDescription deviceDescription, DeviceDescriptionScorer scorer) {
        //Try to find a capability that matches the name and is of type string
        Optional<DeviceDescriptionCapability> capability =
                this.findCapability(deviceDescription, DeviceDescriptionCapability::isString);

        //Check if capability was found and determine the score increment using the operator
        if (capability.isPresent() && this.operator.apply(capability.get().getValueAsString(), this.match)) {
            //Capability was found and operator matches
            return this.scoreIncrement;
        }

        //No match
        return 0;
    }
}
