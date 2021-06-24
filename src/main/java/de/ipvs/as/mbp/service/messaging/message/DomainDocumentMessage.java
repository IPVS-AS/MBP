package de.ipvs.as.mbp.service.messaging.message;

public class DomainDocumentMessage extends DomainMessage<DomainMessageBody> {
    public DomainDocumentMessage(DomainMessageBody messageBody) {
        super(messageBody);
    }
}
