package de.ipvs.as.mbp.domain.discovery.device.scoring.term;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.discovery.operators.StringOperator;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;

/**
 * Objects of this class represent term scoring criteria for devices.
 */
@JsonIgnoreProperties
public class TermScoringCriterion extends ScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "term";

    private TermScoringCriterionField field; //Field to which the criterion is supposed to be applied
    private StringOperator operator; //Operator to use
    private String match; //Match string to use
    private double scoreIncrement; //Increment/decrement of the score

    /**
     * Creates a new term scoring criterion.
     */
    public TermScoringCriterion() {

    }

    /**
     * Creates a new term scoring criterion from a given device description field, a string operator, a match
     * string and a score increment/decrement.
     *
     * @param field          The field of the device description to which this criterion is supposed to be applied
     * @param operator       The {@link StringOperator} to apply to the field
     * @param match          The match string to apply to the field, using the operator
     * @param scoreIncrement The score increment/decrement that is supposed to be added to the score of the device
     *                       description in case of a match.
     */
    @JsonCreator
    public TermScoringCriterion(@JsonProperty("field") TermScoringCriterionField field,
                                @JsonProperty("operator") StringOperator operator,
                                @JsonProperty("match") String match,
                                @JsonProperty("scoreIncrement") double scoreIncrement) {
        //Set fields
        setField(field);
        setOperator(operator);
        setMatch(match);
        setScoreIncrement(scoreIncrement);
    }

    /**
     * Returns the field of the device description to which this criterion is supposed to be applied.
     *
     * @return The field
     */
    public TermScoringCriterionField getField() {
        return field;
    }

    /**
     * Sets the field of the device description to which this criterion is supposed to be applied.
     *
     * @param field The field to set
     * @return THe term scoring criterion
     */
    public TermScoringCriterion setField(TermScoringCriterionField field) {
        this.field = field;
        return this;
    }

    /**
     * Returns the operator that is applied to the field of the device description.
     *
     * @return The operator
     */
    public StringOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator that is applied to the field of the device description.
     *
     * @param operator The operator to set
     * @return THe term scoring criterion
     */
    public TermScoringCriterion setOperator(StringOperator operator) {
        this.operator = operator;
        return this;
    }

    /**
     * Returns the match string that is applied to the field of the device description.
     *
     * @return The match string
     */
    public String getMatch() {
        return match;
    }

    /**
     * Sets the match string that is applied to the field of the device description.
     *
     * @param match The match string to set
     * @return THe term scoring criterion
     */
    public TermScoringCriterion setMatch(String match) {
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
     * @return THe term scoring criterion
     */
    public TermScoringCriterion setScoreIncrement(double scoreIncrement) {
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
        //Check device description field
        if (field == null) {
            exception.addInvalidField(fieldPrefix + ".field", "A device description field must be selected.");
        }

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
        //Retrieve field as string
        String fieldString = this.field.retrieveField(deviceDescription);

        //Apply the operator to the field and return the corresponding score increment
        return this.operator.apply(fieldString, match) ? this.scoreIncrement : 0;
    }
}
