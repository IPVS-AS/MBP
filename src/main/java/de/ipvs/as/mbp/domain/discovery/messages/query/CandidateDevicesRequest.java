package de.ipvs.as.mbp.domain.discovery.messages.query;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.discovery.messages.query.serializer.CandidateDevicesRequirementsSerializer;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.List;
import java.util.Objects;

/**
 * Template for request messages that represent queries for suitable candidate devices with respect to a certain
 * {@link DeviceTemplate}, which are issued against the discovery repositories. For this, the request message contains
 * {@link DeviceRequirement}s and optional {@link ScoringCriterion}s that could be derived from the
 * {@link DeviceTemplate} and describe the desired properties of the candidate devices. Furthermore, the request
 * message may specify  whether a subscription is supposed to be created for this request at the discovery repositories,
 * such that the MBP becomes asynchronously notified when the collection of suitable candidate devices changes over
 * time for the  pertaining {@link DeviceTemplate}.
 */
@DomainMessageTemplate(value = "query", topicSuffix = "query")
public class CandidateDevicesRequest extends DomainMessageBody {
    //The ID of the pertaining device template, serving as identifier for results within replies
    private String referenceId;

    //List of requirements serving as conditions within the query
    @JsonSerialize(using = CandidateDevicesRequirementsSerializer.class)
    private List<DeviceRequirement> requirements;

    //Optional list of scoring criteria for ranking the devices
    private List<ScoringCriterion> scoringCriteria;

    //Topic under which notifications are expected to be published (null = no subscription)
    private String notificationTopic = null;


    /**
     * Creates a new {@link CandidateDevicesRequest} from a given reference ID, a list of {@link DeviceRequirement}s
     * and a list of {@link ScoringCriterion}s, without the intention to register a subscription at the discovery
     * repositories.
     *
     * @param referenceId     The reference ID to use
     * @param requirements    The list of {@link DeviceRequirement}s to use
     * @param scoringCriteria The list of {@link ScoringCriterion}s to use
     */
    public CandidateDevicesRequest(String referenceId, List<DeviceRequirement> requirements, List<ScoringCriterion> scoringCriteria) {
        //Delegate call
        this(referenceId, requirements, scoringCriteria, null);
    }

    /**
     * Creates a new {@link CandidateDevicesRequest} from a given reference ID, a notification topic,
     * a list of {@link DeviceRequirement}s and a list of {@link ScoringCriterion}s. Furthermore, the request message
     * intends to register a subscription at the discovery repositories, such that these repositories publish
     * asynchronous notification messages under the provided notification topic, informing about about changes
     * regarding the collection of suitable candidate devices.
     *
     * @param referenceId       The reference ID to use
     * @param requirements      The list of {@link DeviceRequirement}s to use
     * @param scoringCriteria   The list of {@link ScoringCriterion}s to use
     * @param notificationTopic The notification topic to use
     */
    public CandidateDevicesRequest(String referenceId, List<DeviceRequirement> requirements, List<ScoringCriterion> scoringCriteria, String notificationTopic) {
        //Set fields
        setReferenceId(referenceId);
        setRequirements(requirements);
        setScoringCriteria(scoringCriteria);
        setNotificationTopic(notificationTopic);
    }


    /**
     * Returns the reference ID of the {@link CandidateDevicesRequest}, serving as identifier for query results within
     * reply messages.
     *
     * @return The reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the reference ID of the {@link CandidateDevicesRequest}, serving as identifier for query results within
     * reply messages.
     *
     * @param referenceId The reference ID to set
     */
    public void setReferenceId(String referenceId) {
        //Sanity check
        if ((referenceId == null) || referenceId.isEmpty()) {
            throw new IllegalArgumentException("The reference ID must not be null or empty.");
        }

        this.referenceId = referenceId;
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
     */
    public void setNotificationTopic(String notificationTopic) {
        //Sanity check
        if ((notificationTopic != null) && notificationTopic.isEmpty())
            throw new IllegalArgumentException("The notification topic must not be empty.");


        this.notificationTopic = notificationTopic;
    }

    /**
     * Returns the {@link DeviceRequirement}s of the {@link CandidateDevicesRequest}.
     *
     * @return The list of {@link DeviceRequirement}s
     */
    public List<DeviceRequirement> getRequirements() {
        return requirements;
    }

    /**
     * Sets the {@link DeviceRequirement}s of the {@link CandidateDevicesRequest}.
     *
     * @param requirements The list of {@link DeviceRequirement}s to set
     */
    public void setRequirements(List<DeviceRequirement> requirements) {
        //Null checks
        if ((requirements == null) || requirements.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The requirements must not be null.");
        }

        this.requirements = requirements;
    }

    /**
     * Returns the {@link ScoringCriterion}s of the {@link CandidateDevicesRequest}.
     *
     * @return The list of {@link ScoringCriterion}s
     */
    public List<ScoringCriterion> getScoringCriteria() {
        return scoringCriteria;
    }

    /**
     * Sets the {@link ScoringCriterion}s of the {@link CandidateDevicesRequest}.
     *
     * @param scoringCriteria The list of {@link ScoringCriterion}s to set
     */
    public void setScoringCriteria(List<ScoringCriterion> scoringCriteria) {
        //Null checks
        if ((scoringCriteria == null) || scoringCriteria.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The scoring criteria must not be null.");
        }

        this.scoringCriteria = scoringCriteria;
    }
}
