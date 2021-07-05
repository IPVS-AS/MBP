package de.ipvs.as.mbp.domain.discovery.messages.query;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.scoring.ScoringCriterion;
import de.ipvs.as.mbp.domain.discovery.messages.query.serializer.DeviceQueryRequirementsSerializer;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Request message that is supposed to query the discovery repositories for their registered device descriptions
 * by using various requirements as conditions and optionally various scoring criteria for ranking them.
 * Furthermore, it can be specified whether asynchronous subscriptions for changes in the result set of the query
 * are supposed to be created on the repositories.
 */
@DomainMessageTemplate(value = "device_query", topicSuffix = "query")
public class DeviceQueryRequest extends DomainMessageBody {
    //List of requirements that serve as conditions within the query
    @JsonSerialize(using = DeviceQueryRequirementsSerializer.class)
    private List<DeviceRequirement> requirements;

    //Optional list of scoring criteria to rank the devices
    private List<ScoringCriterion> scoringCriteria;

    //Indicates whether a subscription for asynchronous notifications is supposed to be created
    private RepositorySubscriptionDetails subscription = null;

    /**
     * Creates a new, empty device query request message.
     */
    public DeviceQueryRequest() {
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
     * @return The device query request
     */
    public DeviceQueryRequest setRequirements(List<DeviceRequirement> requirements) {
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
     * @return The device query request
     */
    public DeviceQueryRequest setScoringCriteria(List<ScoringCriterion> scoringCriteria) {
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
     * Sets the subscription details. If null, no subscription will be cretaed.
     *
     * @param subscription The subscription details to set
     * @return The device query request
     */
    public DeviceQueryRequest setSubscription(RepositorySubscriptionDetails subscription) {
        this.subscription = subscription;
        return this;
    }
}
