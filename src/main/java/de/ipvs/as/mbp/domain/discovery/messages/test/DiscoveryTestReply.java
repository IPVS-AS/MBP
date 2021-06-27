package de.ipvs.as.mbp.domain.discovery.messages.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

/**
 * Reply message that is supposed to be received in response to {@link DiscoveryTestRequest} messages. It indicates
 * the availability of an external discovery repository and contains the number of device descriptions the repository
 * holds.
 */
@DomainMessageTemplate("discovery_test_reply")
public class DiscoveryTestReply extends DomainMessageBody {
    //Number of device descriptions the repository contains
    private int devicesCount;

    /**
     * Creates a new discovery test reply message from a given number of known devices.
     *
     * @param devicesCount The number of device descriptions the repository contains
     */
    @JsonCreator
    public DiscoveryTestReply(@JsonProperty("devicesCount") int devicesCount) {
        this.devicesCount = devicesCount;
    }

    /**
     * Returns the number of device descriptions.
     *
     * @return The number of device descriptions
     */
    public int getDevicesCount() {
        return devicesCount;
    }

    /**
     * Sets the number of device descriptions
     *
     * @param devicesCount The number of device descriptions to set
     * @return The discovery test reply message
     */
    public DiscoveryTestReply setDevicesCount(int devicesCount) {
        this.devicesCount = devicesCount;
        return this;
    }
}
