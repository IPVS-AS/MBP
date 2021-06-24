package de.ipvs.as.mbp.service.messaging.message.request;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;

import java.util.UUID;

public class RequestMessage<T extends RequestMessageBody> extends DomainMessage<T> {
    private String correlationId;
    private String returnTopic;

    public RequestMessage(T messageBody, String returnTopic) {
        super(messageBody);
        setReturnTopic(returnTopic);
        generateCorrelationId();
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public RequestMessage<T> setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    public String getReturnTopic() {
        return returnTopic;
    }

    public RequestMessage<T> setReturnTopic(String returnTopic) {
        this.returnTopic = returnTopic;
        return this;
    }

    private void generateCorrelationId() {
        this.correlationId = UUID.randomUUID().toString().replace("-", "");
    }
}
