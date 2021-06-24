package de.ipvs.as.mbp.service.messaging.message.reply;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;

public class ReplyMessage<T extends ReplyMessageBody> extends DomainMessage<T> {

    private String correlationId;
    private String senderId;

    public ReplyMessage() {

    }

    public ReplyMessage(T messageBody) {
        super(messageBody);
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public ReplyMessage<T> setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public String getSenderId() {
        return senderId;
    }

    public ReplyMessage<T> setSenderId(String senderId) {
        this.senderId = senderId;
        return this;
    }
}
