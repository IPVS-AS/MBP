package de.ipvs.as.mbp.domain.discovery.device.scoring.proximity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.SimpleEntityResolver;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionGeoPoint;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionLocation;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.discovery.device.location.point.PointLocationTemplate;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.discovery.LocationTemplateRepository;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDeviceScorer;
import de.ipvs.as.mbp.util.GeoUtils;

import java.util.Optional;

/**
 * Objects of this class represent proximity scoring criteria for devices.
 */
@JsonIgnoreProperties
public class ProximityScoringCriterion extends ScoringCriterion {
    //Type name of this scoring criterion
    private static final String TYPE_NAME = "proximity";

    //ID of the location template to use
    private String locationTemplateId;

    //Maximum score to assign for very close proximity
    private double maximumScore;

    //Distance (in meters) at which half of the score should be assigned
    private double halfScoreDistance;

    /**
     * Creates a new description scoring criterion.
     */
    public ProximityScoringCriterion() {

    }

    /**
     * Creates a new description scoring criterion from a given query string and a score increment/decrement that
     * is supposed to be assigned to a device with a description that exactly matches the query.
     */
    @JsonCreator
    public ProximityScoringCriterion(@JsonProperty("locationTemplateId") String locationTemplateId,
                                     @JsonProperty("maximumScore") double maximumScore,
                                     @JsonProperty("halfScoreDistance") double halfScoreDistance) {
        //Set fields
        setLocationTemplateId(locationTemplateId);
        setMaximumScore(maximumScore);
        setHalfScoreDistance(halfScoreDistance);
    }

    /**
     * Returns the ID of the {@link PointLocationTemplate} that is supposed to be used as origin within this proximity
     * scoring criterion.
     *
     * @return The ID of the location template
     */
    public String getLocationTemplateId() {
        return locationTemplateId;
    }

    /**
     * Returns the {@link PointLocationTemplate} that is supposed to be used as origin within this proximity
     * scoring criterion.
     *
     * @return The ID of the location template
     */
    @JsonIgnore
    public PointLocationTemplate getLocationTemplate() {
        return resolveLocationTemplate(this.locationTemplateId);
    }

    /**
     * Sets the ID of the {@link PointLocationTemplate} that is supposed to be used as origin within this proximity
     * scoring criterion.
     *
     * @param locationTemplateId The ID of the location template to set
     * @return The proximity scoring criterion
     */
    public ProximityScoringCriterion setLocationTemplateId(String locationTemplateId) {
        this.locationTemplateId = locationTemplateId;
        return this;
    }

    /**
     * Returns the maximum score value that is supposed to be assigned to devices which are in very close proximity
     * to the origin.
     *
     * @return The maximum score value
     */
    public double getMaximumScore() {
        return maximumScore;
    }

    /**
     * Sets the maximum score value that is supposed to be assigned to devices which are in very close proximity
     * to the origin.
     *
     * @param maximumScore The maximum score value to set
     * @return The proximity scoring criterion
     */
    public ProximityScoringCriterion setMaximumScore(double maximumScore) {
        this.maximumScore = maximumScore;
        return this;
    }

    /**
     * Returns the distance (in meters) to the origin at which half of the maximum score value is supposed to be
     * assigned to the devices.
     *
     * @return The half score distance (meters)
     */
    public double getHalfScoreDistance() {
        return halfScoreDistance;
    }

    /**
     * Sets the distance (in meters) to the origin at which half of the maximum score value is supposed to be
     * assigned to the devices.
     *
     * @param halfScoreDistance The half score distance to set (meters)
     * @return The proximity scoring criterion
     */
    public ProximityScoringCriterion setHalfScoreDistance(double halfScoreDistance) {
        this.halfScoreDistance = halfScoreDistance;
        return this;
    }

    /**
     * Returns the name of the scoring criteria.
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
        //Check if location template exists and if it is of the desired type
        if ((this.locationTemplateId == null) || (this.locationTemplateId.isEmpty()) || getLocationTemplate() == null) {
            exception.addInvalidField(fieldPrefix + ".locationTemplateId", "The referenced location template does not exist or is not of type 'point'.");
        }

        //Check maximum score
        if (this.maximumScore == 0) {
            exception.addInvalidField(fieldPrefix + ".maximumScore", "The maximum score must not be zero,");
        }

        //Check half score distance
        if (this.halfScoreDistance <= 0) {
            exception.addInvalidField(fieldPrefix + ".halfScoreDistance", "The half score distance must be greater than zero.");
        }
    }

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
    @Override
    public double getScoreIncrement(DeviceDescription deviceDescription, CandidateDeviceScorer scorer) {
        //Retrieve location information of the device and the location template
        DeviceDescriptionLocation deviceLocation = deviceDescription.getLocation();
        PointLocationTemplate locationTemplate = getLocationTemplate();

        //Check if coordinates are available and if the location template is valid
        if ((deviceLocation == null) || (deviceLocation.getCoordinates() == null) || (locationTemplate == null)) {
            //Missing information
            return 0;
        }

        //Calculate distance between location template and device location
        DeviceDescriptionGeoPoint deviceCoordinates = deviceLocation.getCoordinates();
        double distance = GeoUtils.getGeoDistance(locationTemplate.getLatitude(), locationTemplate.getLongitude(),
                deviceCoordinates.getLatitude(), deviceCoordinates.getLongitude());

        /*
        Transformation: At this point, the distance needs to be transformed to a score by making use of the
        user-provided input. The following approach (with distance d >= 0) is used, resulting in a limited
        decrease function that strives against the asymptote of zero:
        score(d) = max_score * a^d (when d > 0, this is equivalent to score(d) = max_score * exp(ln(a) * d))

        For the half score distance h > 0 it is known:
        score(h) = max_score * a^h = max_score / 2
        <=> a^h = 1/2                                (now exponentiating both sides of the equation by (1/h))
        <=> a = (1/2)^(1/h) = 2^(-1/h)

        From this result it can already be seen that 0 < a < 1 which in the end will result in a decrease function.
        When putting all together, the following transformation function results:
        score(d) = max_score * 2^(-d/h)

        This formula can be easily validated when using h as input, since this leads to
        score(h) = max_score * 2^(-h/h) = max_score * 2^(-1) = max_score / 2
        while
        score(0) = max_score * 2^(0/h) = max_score
        and
        lim(d -> inf) max_score * 2^(-d/h) = lim(d -> inf) max_score * 0 = 0
        */

        //Transform the calculated distance to a score using the formula from above:
        return this.maximumScore * Math.pow(2, (-distance) / this.halfScoreDistance);
    }

    /**
     * Resolves the referenced {@link PointLocationTemplate} from the database.
     *
     * @param locationTemplateId The ID of the location template to resolve
     * @return The location template
     */
    private PointLocationTemplate resolveLocationTemplate(String locationTemplateId) {
        //Resolve location template
        Optional<Object> template = SimpleEntityResolver.resolve(LocationTemplateRepository.class, locationTemplateId);

        //Check if template was found and is of type point
        if ((!template.isPresent()) || (!(template.get() instanceof PointLocationTemplate))) {
            return null;
        }

        //Cast the location template and return it
        return (PointLocationTemplate) template.get();
    }
}
