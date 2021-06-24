package de.ipvs.as.mbp.domain.discovery.messages.test;

import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;
import de.ipvs.as.mbp.service.messaging.message.reply.ReplyMessageBody;

@DomainMessageTemplate("discovery_test_reply")
public class DiscoveryTestReply extends ReplyMessageBody {
    private int numDevices;

    public DiscoveryTestReply() {

    }

    public int getNumDevices() {
        return numDevices;
    }

    public DiscoveryTestReply setNumDevices(int numDevices) {
        this.numDevices = numDevices;
        return this;
    }
}
