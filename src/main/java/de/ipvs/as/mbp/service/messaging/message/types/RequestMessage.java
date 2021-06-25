package de.ipvs.as.mbp.service.messaging.message.types;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;

public class RequestMessage<T extends DomainMessageBody> extends DomainMessage<T> {
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
