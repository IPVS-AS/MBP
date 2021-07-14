package de.ipvs.as.mbp.domain.discovery.device.scoring;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDeviceScorer;

/**
 * Abstract base class for scoring criteria for devices.
 */
public abstract class ScoringCriterion {
    /**
     * Creates a new scoring criterion.
     */
    public ScoringCriterion() {

    }

    /**
     * Returns the name of the scoring criteria.
     *
     * @return The name
     */
    public abstract String getTypeName();

    /**
     * Validates the device scoring criterion by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    public abstract void validate(EntityValidationException exception, String fieldPrefix);

    /**
     * Applies the scoring criterion to a given {@link DeviceDescription} and returns the resulting scoring increment
     * (positive number) or scoring decrement (negative number) for this description as result. In addition, a
     * reference to the {@link CandidateDeviceScorer} that currently performs the overall score calculations is
     * provided, which may contain additional information about the collection of {@link DeviceDescription}s that are
     * currently subject to the scoring process and thus enables the calculation of relative scores.
     *
     * @param deviceDescription The device description for which the score increment of this scoring criterion is
     *                          supposed to be calculated
     * @param scorer            The {@link CandidateDeviceScorer} that currently performs the overall score
     *                          calculations for a collection of {@link DeviceDescription}s.
     * @return The score increment/decrement that results from the application of this scoring criterion to the given
     * device description
     */
    public abstract double getScoreIncrement(DeviceDescription deviceDescription, CandidateDeviceScorer scorer);
}
