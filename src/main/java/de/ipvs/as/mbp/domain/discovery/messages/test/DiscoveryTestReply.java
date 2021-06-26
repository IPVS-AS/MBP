package de.ipvs.as.mbp.domain.discovery.messages.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

/**
 * Reply message that is supposed to be received in response to {@link DiscoveryTestRequest} messages and indicates
 * the availability of a discovery gateway as well as the number of devices it knows about.
 */
@DomainMessageTemplate("discovery_test_reply")
public class DiscoveryTestReply extends DomainMessageBody {
    //Number of devices the gateway knows about
    private int devicesCount;

    /**
     * Creates a new discovery test reply message from a given number of known devices.
     *
     * @param devicesCount The number of devices the gateway knows about
     */
    @JsonCreator
    public DiscoveryTestReply(@JsonProperty("devicesCount") int devicesCount) {
        this.devicesCount = devicesCount;
    }

    /**
     * Returns the number of known devices.
     *
     * @return The number of devices
     */
    public int getDevicesCount() {
        return devicesCount;
    }

    /**
     * Sets the number of known devices
     *
     * @param devicesCount The number of devices to set
     * @return The discovery test reply message
     */
    public DiscoveryTestReply setDevicesCount(int devicesCount) {
        this.devicesCount = devicesCount;
        return this;
    }
}
