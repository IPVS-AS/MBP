package de.ipvs.as.mbp.domain.discovery.messages.test;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

@DomainMessageTemplate("discovery_test_reply")
public class DiscoveryTestReply extends DomainMessageBody {
    private int numDevices;

    @JsonCreator
    public DiscoveryTestReply(@JsonProperty("numDevices") int numDevices) {
        this.numDevices = numDevices;
    }

    public int getNumDevices() {
        return numDevices;
    }

    public DiscoveryTestReply setNumDevices(int numDevices) {
        this.numDevices = numDevices;
        return this;
    }
}
