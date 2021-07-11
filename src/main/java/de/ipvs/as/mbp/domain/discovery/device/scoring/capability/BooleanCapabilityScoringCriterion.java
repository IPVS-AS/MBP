package de.ipvs.as.mbp.domain.discovery.device.scoring.capability;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionCapability;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;

import java.util.Optional;

/**
 * Objects of this class represent scoring criteria for capabilities of devices that are expressed as booleans.
 */
@JsonIgnoreProperties
public class BooleanCapabilityScoringCriterion extends CapabilityScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "boolean_capability";

    //Increment/decrement of the score when true
    private double trueScoreIncrement;

    //Increment/decrement of the score when false
    private double falseScoreIncrement;

    /**
     * Creates a new scoring criterion for a capability that is expressed as boolean.
     */
    public BooleanCapabilityScoringCriterion() {

    }

    /**
     * Creates a new scoring criterion for a capability that is expressed as boolean by using the name of the pertaining
     * capability and score increments/decrements for the cases that the capability value is true or false.
     *
     * @param capabilityName      The name of the capability to which this criterion is supposed to be applied
     * @param trueScoreIncrement  The score increment/decrement that is supposed to be added to the score of the device
     *                            description in case the capability value is true
     * @param falseScoreIncrement The score increment/decrement that is supposed to be added to the score of the device
     *                            description in case the capability value is false
     */
    @JsonCreator
    public BooleanCapabilityScoringCriterion(@JsonProperty("capabilityName") String capabilityName,
                                             @JsonProperty("trueScoreIncrement") double trueScoreIncrement,
                                             @JsonProperty("falseScoreIncrement") double falseScoreIncrement) {
        //Set fields
        super(capabilityName);
        setTrueScoreIncrement(trueScoreIncrement);
        setFalseScoreIncrement(falseScoreIncrement);
    }

    /**
     * Returns the score increment (positive number) or decrement (negative number) that is supposed to be added
     * to the score of the device description in case the capability value is true.
     *
     * @return The score increment for true values
     */
    public double getTrueScoreIncrement() {
        return trueScoreIncrement;
    }

    /**
     * Sets the score increment (positive number) or decrement (negative number) that is supposed to be added
     * to the score of the device description in case the capability value is true.
     *
     * @param trueScoreIncrement The score increment for true values to set
     * @return The boolean capability scoring criterion
     */
    public BooleanCapabilityScoringCriterion setTrueScoreIncrement(double trueScoreIncrement) {
        this.trueScoreIncrement = trueScoreIncrement;
        return this;
    }

    /**
     * Returns the score increment (positive number) or decrement (negative number) that is supposed to be added
     * to the score of the device description in case the capability value is false.
     *
     * @return The score increment for false values
     */
    public double getFalseScoreIncrement() {
        return falseScoreIncrement;
    }

    /**
     * Sets the score increment (positive number) or decrement (negative number) that is supposed to be added
     * to the score of the device description in case the capability value is false.
     *
     * @param falseScoreIncrement The score increment for false values to set
     * @return The boolean capability scoring criterion
     */
    public BooleanCapabilityScoringCriterion setFalseScoreIncrement(double falseScoreIncrement) {
        this.falseScoreIncrement = falseScoreIncrement;
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
        //Try to find a capability that matches the name and is of type boolean
        Optional<DeviceDescriptionCapability> capability =
                this.findCapability(deviceDescription, DeviceDescriptionCapability::isBoolean);

        //Evaluate capability value if capability was found
        return capability.map(c -> c.getValueAsBoolean() ? this.trueScoreIncrement : this.falseScoreIncrement)
                .orElse(0.0);
    }
}
