package de.ipvs.as.mbp.domain.discovery.messages.cancel;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.messages.query.CandidateDevicesRequest;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.HashSet;
import java.util.Set;

/**
 * Request message that is supposed to cancel existing subscriptions at a discovery repository that have been
 * previously created in response to {@link CandidateDevicesRequest}s for {@link DeviceTemplate}s.
 * After cancelling the subscriptions, no further asynchronous notifications about changes in the collection of
 * suitable candidate devices for the pertaining {@link DeviceTemplate}s will be sent by the repository anymore.
 * No reply is expected from the receiving discovery repositories in response to this message.
 */
@DomainMessageTemplate(value = "cancel_subscriptions", topicSuffix = "cancel")
public class CancelSubscriptionsMessage extends DomainMessageBody {
    //Set containing the reference IDs of the device templates for which the subscriptions are supposed to be cancelled
    private Set<String> referenceIds;

    /**
     * Creates a new, empty cancel subscription message.
     */
    public CancelSubscriptionsMessage() {
        //Initialize data structures
        this.referenceIds = new HashSet<>();
    }

    /**
     * Creates a new cancel subscription message from a given reference ID.
     *
     * @param referenceId The reference ID of the subscriptions to cancel
     */
    public CancelSubscriptionsMessage(String referenceId) {
        this();
        addReferenceId(referenceId);
    }

    /**
     * Creates a new cancel subscription message from a given set of reference IDs.
     *
     * @param referenceId The set of reference IDs of the subscriptions to cancel
     */
    public CancelSubscriptionsMessage(Set<String> referenceId) {
        this();
        setReferenceIds(referenceId);
    }

    /**
     * Returns the set of reference IDs that are used in order to identify the subscriptions that are supposed to be
     * cancelled.
     *
     * @return The set of reference IDs
     */
    public Set<String> getReferenceIds() {
        return this.referenceIds;
    }

    /**
     * Sets the set of reference IDs that are used in order to identify the subscriptions that are supposed to be
     * cancelled.
     *
     * @param referenceIds The set of reference IDs to set
     * @return The cancel subscription message
     */
    public CancelSubscriptionsMessage setReferenceIds(Set<String> referenceIds) {
        //Null checks
        if ((referenceIds == null) || referenceIds.stream().anyMatch(s -> (s == null) || s.isEmpty())) {
            throw new IllegalArgumentException("The reference IDs must not be null or empty.");
        }

        this.referenceIds = referenceIds;
        return this;
    }

    /**
     * Adds a given reference ID that can be used in order to identify a subscription that is supposed to be cancelled.
     *
     * @param referenceId The reference ID to add
     * @return The cancel subscription message
     */
    public CancelSubscriptionsMessage addReferenceId(String referenceId) {
        //Sanity checks
        if ((referenceId == null) || referenceId.isEmpty())
            throw new IllegalStateException("The reference ID must not be null or empty.");

        //Add it
        this.referenceIds.add(referenceId);
        return this;
    }
}
