package de.ipvs.as.mbp.domain.discovery.messages.query;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.discovery.messages.query.serializer.DeviceQueryRequirementsSerializer;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Request message that is supposed to request a collection of suitable candidate devices that match a certain
 * {@link DeviceTemplate} from the discovery repositories. For this, requirements and scoring criteria that
 * were derived from the {@link DeviceTemplate} can be added to the body of the request message, such that the
 * repositories can determine matching devices, optionally score and rank them based on the given criteria
 * and send the descriptions of the determined devices back as reply.
 * Furthermore, it can be specified in the request message whether asynchronous subscriptions are supposed to be
 * created on the repositories. By using them, the MBP can become notified when the collection of suitable candidate
 * devices for the {@link DeviceTemplate} changes over time at the corresponding repository.
 */
@DomainMessageTemplate(value = "query", topicSuffix = "query")
public class CandidateDevicesRequest extends DomainMessageBody {
    //List of requirements that serve as conditions within the query
    @JsonSerialize(using = DeviceQueryRequirementsSerializer.class)
    private List<DeviceRequirement> requirements;

    //Optional list of scoring criteria to rank the devices
    private List<ScoringCriterion> scoringCriteria;

    //Indicates whether a subscription for asynchronous notifications is supposed to be created
    private RepositorySubscriptionDetails subscription = null;

    /**
     * Creates a new, empty candidate devices request message.
     */
    public CandidateDevicesRequest() {
        //Initialize lists
        this.requirements = new ArrayList<>();
        this.scoringCriteria = new ArrayList<>();
    }

    /**
     * Returns the list of requirements.
     *
     * @return The list of requirements
     */
    public List<DeviceRequirement> getRequirements() {
        return requirements;
    }

    /**
     * Sets the list of requirements.
     *
     * @param requirements The list of requirements to set
     * @return The candidate devices request
     */
    public CandidateDevicesRequest setRequirements(List<DeviceRequirement> requirements) {
        this.requirements = requirements;
        return this;
    }

    /**
     * Returns the list of scoring criteria.
     *
     * @return The list of scoring criteria
     */
    public List<ScoringCriterion> getScoringCriteria() {
        return scoringCriteria;
    }

    /**
     * Sets the list of scoring criteria
     *
     * @param scoringCriteria The list of scoring criteria to set
     * @return The candidate devices request
     */
    public CandidateDevicesRequest setScoringCriteria(List<ScoringCriterion> scoringCriteria) {
        this.scoringCriteria = scoringCriteria;
        return this;
    }

    /**
     * Returns the subscription details or null, if no subscription is desired.
     *
     * @return The subscription details
     */
    public RepositorySubscriptionDetails getSubscription() {
        return subscription;
    }

    /**
     * Sets the subscription details. If null, no subscription will be created.
     *
     * @param subscription The subscription details to set
     * @return The candidate devices request
     */
    public CandidateDevicesRequest setSubscription(RepositorySubscriptionDetails subscription) {
        this.subscription = subscription;
        return this;
    }
}
