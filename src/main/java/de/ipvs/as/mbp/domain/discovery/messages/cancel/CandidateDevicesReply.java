package de.ipvs.as.mbp.domain.discovery.messages.cancel;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.messages.query.RepositorySubscriptionDetails;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.List;

/**
 * Reply message that is supposed to be received in response to {@link CancelSubscriptionMessage} messages. It contains
 * the {@link DeviceDescription}s of all suitable candidate devices that could be determined at the repository
 * for the corresponding {@link DeviceTemplate} that was used for deriving the requirements and scoring criteria
 * of the the preceding request. This message may be either received in a synchronous manner as direct response to
 * {@link CancelSubscriptionMessage}s or as asynchronous notification about changes in the collection
 * of candidate devices as part of a subscription. In the latter case, a reference ID is provided matching the
 * reference ID of the {@link RepositorySubscriptionDetails} object that was part of the original
 * {@link CancelSubscriptionMessage}.
 */
@DomainMessageTemplate(value = "device_query_reply")
public class CandidateDevicesReply extends DomainMessageBody {
    //Reference ID matching the one that was provided in the subscription details (if needed)
    private String referenceId;

    //List of device descriptions of the suitable candidate devices
    private List<DeviceDescription> deviceDescriptions;

    /**
     * Creates a new candidate devices reply.
     */
    public CandidateDevicesReply() {

    }

    /**
     * Returns the reference ID or null, if none is provided.
     *
     * @return The reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Returns the list of {@link DeviceDescription}s for the suitable candidate devices that were determined
     * at a repository.
     *
     * @return The list of device descriptions
     */
    public List<DeviceDescription> getDeviceDescriptions() {
        return deviceDescriptions;
    }
}
