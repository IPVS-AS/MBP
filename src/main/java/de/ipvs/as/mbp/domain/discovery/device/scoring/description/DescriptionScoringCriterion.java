package de.ipvs.as.mbp.domain.discovery.device.scoring.description;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.DeviceDescriptionScorer;

/**
 * Objects of this class represent description scoring criteria for devices.
 */
@JsonIgnoreProperties
public class DescriptionScoringCriterion extends ScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "description";

    private String query; //The textual query against the description to use
    private double exactMatchScore; //Score for exact matches

    /**
     * Creates a new description scoring criterion.
     */
    public DescriptionScoringCriterion() {

    }

    /**
     * Creates a new description scoring criterion from a given query string and a score increment/decrement that
     * is supposed to be assigned to a device with a description that exactly matches the query.
     *
     * @param query           The textual query against the description of the device
     * @param exactMatchScore The score that is supposed to be assigned to exactly matching descriptions
     */
    @JsonCreator
    public DescriptionScoringCriterion(@JsonProperty("query") String query,
                                       @JsonProperty("exactMatchScore") double exactMatchScore) {
        //Set fields
        setQuery(query);
        setExactMatchScore(exactMatchScore);
    }

    /**
     * Returns the textual query that is supposed to be evaluated against the description of the device.
     *
     * @return The query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the textual query that is supposed to be evaluated against the description of the device.
     *
     * @param query The query to set
     * @return THe description scoring criterion
     */
    public DescriptionScoringCriterion setQuery(String query) {
        this.query = query;
        return this;
    }

    /**
     * Returns the score value that is supposed to be assigned to devices whose descriptions exactly match the query.
     *
     * @return The exact match score
     */
    public double getExactMatchScore() {
        return exactMatchScore;
    }

    /**
     * Sets the score value that is supposed to be assigned to devices whose descriptions exactly match the query.
     *
     * @param exactMatchScore The exact match score to set
     * @return THe description scoring criterion
     */
    public DescriptionScoringCriterion setExactMatchScore(double exactMatchScore) {
        this.exactMatchScore = exactMatchScore;
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
        //Check query string
        if ((query == null) || (query.isEmpty())) {
            exception.addInvalidField(fieldPrefix + ".query", "The query must not be empty.");
        }

        //Check exact match score
        if (exactMatchScore <= 0) {
            exception.addInvalidField(fieldPrefix + ".exactMatchScore", "The score for exact matches must be greater than zero.");
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
        //TODO
        return 0;
    }
}
