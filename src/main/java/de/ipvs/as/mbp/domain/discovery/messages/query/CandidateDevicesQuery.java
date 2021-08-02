package de.ipvs.as.mbp.domain.discovery.messages.query;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.discovery.messages.query.serializer.DeviceQueryRequirementsSerializer;

import java.util.List;
import java.util.Objects;

/**
 * Objects of this class represent queries for candidate devices which are supposed to request collections of suitable
 * candidate devices, matching a certain {@link DeviceTemplate}, from the discovery repositories. For this,
 * each {@link CandidateDevicesQuery} specifies {@link DeviceRequirement}s and optional {@link ScoringCriterion}s
 * that were derived from the corresponding {@link DeviceTemplate} and describe the desired properties of the
 * candidate devices. Furthermore, a query may specify whether a subscription is supposed to be created for this
 * query at the discovery repositories, such that the MBP becomes asynchronously notified when the collection of
 * suitable candidate devices changes over time for the pertaining {@link DeviceTemplate}.
 */
public class CandidateDevicesQuery {
    //The ID of the pertaining device template, serving as identifier for results within replies
    private String referenceId;

    //List of requirements serving as conditions within the query
    @JsonSerialize(using = DeviceQueryRequirementsSerializer.class)
    private List<DeviceRequirement> requirements;

    //Optional list of scoring criteria for ranking the devices
    private List<ScoringCriterion> scoringCriteria;

    //Topic under which notifications are expected to be published (null = no subscription)
    private String notificationTopic = null;

    /**
     * Creates a new {@link CandidateDevicesQuery} from a given reference ID, a list of {@link DeviceRequirement}s,
     * and a list of {@link ScoringCriterion}s, without the intention to register a subscription at the discovery
     * repositories.
     *
     * @param referenceId     The reference ID to use
     * @param requirements    The list of {@link DeviceRequirement}s to use
     * @param scoringCriteria The list of {@link ScoringCriterion}s to use
     */
    public CandidateDevicesQuery(String referenceId, List<DeviceRequirement> requirements, List<ScoringCriterion> scoringCriteria) {
        //Delegate call
        this(referenceId, requirements, scoringCriteria, null);
    }

    /**
     * Creates a new {@link CandidateDevicesQuery} from a given reference ID, a notification topic,
     * a list of {@link DeviceRequirement}s and a list of {@link ScoringCriterion}s. Furthermore, the
     * {@link CandidateDevicesQuery} intends to register a subscription at the discovery repositories, such that these
     * repositories publish asynchronous notification messages about changes regarding the candidate devices under
     * the provided notification topic.
     *
     * @param referenceId       The reference ID to use
     * @param requirements      The list of {@link DeviceRequirement}s to use
     * @param scoringCriteria   The list of {@link ScoringCriterion}s to use
     * @param notificationTopic The notification topic to use
     */
    public CandidateDevicesQuery(String referenceId, List<DeviceRequirement> requirements, List<ScoringCriterion> scoringCriteria, String notificationTopic) {
        //Set fields
        setReferenceId(referenceId);
        setRequirements(requirements);
        setScoringCriteria(scoringCriteria);
        setNotificationTopic(notificationTopic);
    }

    /**
     * Returns the reference ID of the {@link CandidateDevicesQuery}, serving as identifier for query results within
     * reply messages.
     *
     * @return The reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the reference ID of the {@link CandidateDevicesQuery}, serving as identifier for query results within
     * reply messages.
     *
     * @param referenceId The reference ID to set
     * @return The query
     */
    public CandidateDevicesQuery setReferenceId(String referenceId) {
        //Sanity check
        if ((referenceId == null) || referenceId.isEmpty()) {
            throw new IllegalArgumentException("The reference ID must not be null or empty.");
        }

        this.referenceId = referenceId;
        return this;
    }

    /**
     * Returns the topic under which the asynchronous notifications as result to the subscription at the discovery
     * repository are expected to be published.
     *
     * @return The notification topic or null, if no subscription is registered
     */
    public String getNotificationTopic() {
        return notificationTopic;
    }

    /**
     * Sets the topic under which the asynchronous notifications as result to the subscription at the discovery
     * repository are expected to be published.
     *
     * @param notificationTopic The notification topic to set or null, if no subscription is registered
     * @return The query
     */
    public CandidateDevicesQuery setNotificationTopic(String notificationTopic) {
        //Sanity check
        if (notificationTopic.isEmpty())
            throw new IllegalArgumentException("The notification topic must not be empty.");


        this.notificationTopic = notificationTopic;
        return this;
    }

    /**
     * Returns the {@link DeviceRequirement}s of the {@link CandidateDevicesQuery}.
     *
     * @return The list of {@link DeviceRequirement}s
     */
    public List<DeviceRequirement> getRequirements() {
        return requirements;
    }

    /**
     * Sets the {@link DeviceRequirement}s of the {@link CandidateDevicesQuery}.
     *
     * @param requirements The list of {@link DeviceRequirement}s to set
     * @return The query
     */
    public CandidateDevicesQuery setRequirements(List<DeviceRequirement> requirements) {
        //Null checks
        if ((requirements == null) || requirements.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The requirements must not be null.");
        }

        this.requirements = requirements;
        return this;
    }

    /**
     * Returns the {@link ScoringCriterion}s of the {@link CandidateDevicesQuery}.
     *
     * @return The list of {@link ScoringCriterion}s
     */
    public List<ScoringCriterion> getScoringCriteria() {
        return scoringCriteria;
    }

    /**
     * Sets the {@link ScoringCriterion}s of the {@link CandidateDevicesQuery}.
     *
     * @param scoringCriteria The list of {@link ScoringCriterion}s to set
     * @return The query
     */
    public CandidateDevicesQuery setScoringCriteria(List<ScoringCriterion> scoringCriteria) {
        //Null checks
        if ((scoringCriteria == null) || scoringCriteria.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The scoring criteria must not be null.");
        }

        this.scoringCriteria = scoringCriteria;
        return this;
    }
}
