package de.ipvs.as.mbp.service.messaging.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.util.InstantSerializer;
import de.ipvs.as.mbp.util.Json;

import java.time.Instant;

public class DomainMessage<T extends DomainMessageBody> {
    @JsonProperty("type")
    private String typeName;

    @JsonProperty("message")
    private T messageBody;

    @JsonProperty("time")
    @JsonSerialize(using = InstantSerializer.class)
    private Instant timestamp = Instant.now();

    public DomainMessage() {

    }

    public DomainMessage(T messageBody) {
        setMessageBody(messageBody);
    }

    public String getTypeName() {
        return typeName;
    }

    public T getMessageBody() {
        return messageBody;
    }

    public DomainMessage<T> setMessageBody(T messageBody) {
        //Sanity check
        if (messageBody == null) {
            throw new IllegalArgumentException("Message body must not be null.");
        }

        //Get message type name from message body
        this.typeName = getTypeName(messageBody);

        //Set message body
        this.messageBody = messageBody;
        return this;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public DomainMessage<T> setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    private String getTypeName(DomainMessageBody messageBody) {
        //Get class of message body
        Class<? extends DomainMessageBody> messageBodyClass = messageBody.getClass();

        //Check if annotation is available
        requireAnnotation(messageBody);

        //Retrieve message type name from annotation
        return messageBodyClass.getAnnotation(DomainMessageTemplate.class).value();
    }

    private void requireAnnotation(DomainMessageBody messageBody) {
        //Get class of message body
        Class<? extends DomainMessageBody> messageBodyClass = messageBody.getClass();

        //Check if annotation is available
        if (!messageBodyClass.isAnnotationPresent(DomainMessageTemplate.class)) {
            throw new IllegalArgumentException(String.format("The class of the message body must be annotated with the %s annotation.", DomainMessageTemplate.class.getName()));
        }
    }

    /**
     * Uses Jackson to transforms the object to a JSON string and returns the result.
     *
     * @return The resulting JSON string
     */
    @Override
    public String toString() {
        //Transform object to JSON string
        return Json.of(this);
    }
}
