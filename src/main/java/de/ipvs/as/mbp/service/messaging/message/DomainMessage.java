package de.ipvs.as.mbp.service.messaging.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.util.InstantFromEpochMilliDeserializer;
import de.ipvs.as.mbp.util.InstantToEpochMilliSerializer;
import de.ipvs.as.mbp.util.Json;

import java.time.Instant;

/**
 * Base class for domain-specific messages that need to be transmitted via publish-subscribe-based messaging.
 * Each domain message consists out of a type name that allows to identify the type of the message, a message body
 * with contains the actual domain-specific information of interest and a timestamp that specifies when the message
 * was sent. The type name of the message is automatically inferred from a {@link DomainMessageTemplate} annotation
 * that is placed above the {@link DomainMessageBody} class which specifies the structure of the message body.
 * The body of the message can be of an arbitrary type that inherits from {@link DomainMessageBody}.
 *
 * @param <T> The type of the message body
 */
public abstract class DomainMessage<T extends DomainMessageBody> {
    //Type name of the message
    @JsonProperty("type")
    private String typeName;

    //Body of the message
    @JsonProperty("message")
    private T messageBody;

    //Timestamp when the message was sent
    @JsonProperty("time")
    @JsonSerialize(using = InstantToEpochMilliSerializer.class)
    @JsonDeserialize(using = InstantFromEpochMilliDeserializer.class)
    private Instant timestamp = Instant.now();

    /**
     * Creates a new, empty domain message without message body.
     */
    public DomainMessage() {

    }

    /**
     * Creates a new domain message from a given message body.
     *
     * @param messageBody The message body to use
     */
    public DomainMessage(T messageBody) {
        setMessageBody(messageBody);
    }

    /**
     * Creates a new domain message as copy of a existing domain message. However, no copy of the message body is
     * created; instead the same reference is used.
     *
     * @param domainMessage The existing domain message to copy
     */
    public DomainMessage(DomainMessage<T> domainMessage) {
        //Sanity check
        if (domainMessage == null) {
            throw new IllegalArgumentException("The original domain message must not be null when creating a copy.");
        }

        //Copy fields from the provided domain message
        this.typeName = domainMessage.typeName;
        this.messageBody = domainMessage.messageBody;
        this.timestamp = domainMessage.timestamp;
    }

    /**
     * Returns the the type name of the message which allows to identify the message type.
     *
     * @return The type name
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Returns the body of the message which contains the actual information of interest.
     *
     * @return The message body
     */
    public T getMessageBody() {
        return messageBody;
    }

    /**
     * Sets the body of the message which contains the actual information of interest.
     *
     * @param messageBody The message body to set
     * @return The domain message
     */
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

    /**
     * Returns the timestamp of the message which approximately indicates when the message was sent.
     *
     * @return The timestamp
     */
    public Instant getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp of the message which approximately indicates when the message was sent.
     *
     * @param timestamp The timestamp to set
     * @return The domain message
     */
    public DomainMessage<T> setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Sets the timestamp of the message, which approximately indicates when the message was sent, to the current time.
     *
     * @return The domain message
     */
    public DomainMessage<T> updateTimestamp() {
        this.timestamp = Instant.now();
        return this;
    }

    /**
     * Retrieves the type name of the message by inspecting the {@link DomainMessageTemplate} annotation that is placed
     * above the class of the given message body. If no annotation is found, an exception will be thrown.
     *
     * @param messageBody The message body to inspect
     * @return The retrieved type name
     */
    public String getTypeName(DomainMessageBody messageBody) {
        //Retrieve the message type name from the message body
        return messageBody.getTypeName();
    }

    /**
     * Uses Jackson in order to transform the domain messageto a JSON string and returns the result.
     *
     * @return The resulting JSON string
     */
    @Override
    public String toString() {
        //Transform object to JSON string
        return Json.of(this);
    }
}
