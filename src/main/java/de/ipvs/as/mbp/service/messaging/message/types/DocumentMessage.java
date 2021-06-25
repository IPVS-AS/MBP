package de.ipvs.as.mbp.service.messaging.message.types;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;

public class DocumentMessage extends DomainMessage<DomainMessageBody> {
    public DocumentMessage(DomainMessageBody messageBody) {
        super(messageBody);
    }
}
