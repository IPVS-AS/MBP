package de.ipvs.as.mbp.service.messaging.message.request;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;

import java.util.UUID;

public class RequestMessage<T extends RequestMessageBody> extends DomainMessage<T> {
    private String returnTopic;

    public RequestMessage(T messageBody, String returnTopic) {
        super(messageBody);
        setReturnTopic(returnTopic);
    }

    public String getReturnTopic() {
        return returnTopic;
    }

    public RequestMessage<T> setReturnTopic(String returnTopic) {
        this.returnTopic = returnTopic;
        return this;
    }
}
