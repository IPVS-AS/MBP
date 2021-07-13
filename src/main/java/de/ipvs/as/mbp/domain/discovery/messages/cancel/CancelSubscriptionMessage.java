package de.ipvs.as.mbp.domain.discovery.messages.cancel;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.messages.query.CandidateDevicesRequest;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

/**
 * Request message that is supposed to cancel an existing subscription at a discovery repository that has been
 * previously created in response to a {@link CandidateDevicesRequest} for a certain {@link DeviceTemplate}.
 * After cancelling the subscription, no further asynchronous notifications about changes in the collection of
 * suitable candidate devices for the pertaining {@link DeviceTemplate} will be sent by the repository anymore.
 * No reply is expected from the receiving discovery repositories in response to this message.
 */
@DomainMessageTemplate(value = "cancel_subscription", topicSuffix = "cancel")
public class CancelSubscriptionMessage extends DomainMessageBody {
    //ID of the affected device template for which the subscription is supposed to be cancelled
    private String referenceId;

    /**
     * Creates a new, empty cancel subscription message.
     */
    public CancelSubscriptionMessage() {

    }

    /**
     * Returns the reference ID that is used in order to identify the subscription that is supposed to be cancelled.
     *
     * @return The reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the reference ID that is used in order to identify the subscription that is supposed to be cancelled.
     *
     * @param referenceId The reference ID to set
     * @return The cancel subscription message
     */
    public CancelSubscriptionMessage setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }
}
