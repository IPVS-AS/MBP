package de.ipvs.as.mbp.domain.discovery.messages.query;

import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

/**
 * Request message that is supposed to test the availability of discovery repositories.
 */
@DomainMessageTemplate(value = "repository_test")
public class DeviceQueryReply extends DomainMessageBody {

    /**
     * Creates a new discovery test request.
     */
    public DeviceQueryReply() {

    }
}
