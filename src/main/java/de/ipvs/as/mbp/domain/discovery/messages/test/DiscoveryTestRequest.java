package de.ipvs.as.mbp.domain.discovery.messages.test;

import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

/**
 * Request message that is supposed to test the availability of discovery gateways.
 */
@DomainMessageTemplate(value = "discovery_test")
public class DiscoveryTestRequest extends DomainMessageBody {

    /**
     * Creates a new discovery test request.
     */
    public DiscoveryTestRequest() {

    }
}
