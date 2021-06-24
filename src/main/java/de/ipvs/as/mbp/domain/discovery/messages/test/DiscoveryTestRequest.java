package de.ipvs.as.mbp.domain.discovery.messages.test;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;
import de.ipvs.as.mbp.service.messaging.message.request.RequestMessageBody;

/**
 * Request message for testing the availability of discovery gateways.
 */
@DomainMessageTemplate(value = "discovery_test", replyType = DiscoveryTestReply.class)
public class DiscoveryTestRequest extends RequestMessageBody {

    public DiscoveryTestRequest() {

    }
}
