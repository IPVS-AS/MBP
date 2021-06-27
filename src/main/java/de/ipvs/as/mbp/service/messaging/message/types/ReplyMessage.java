package de.ipvs.as.mbp.service.messaging.message.types;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;

/**
 * Objects of this class represent reply messages, i.e. messages that are typically sent to the receiver as response to
 * a preceding {@link RequestMessage}. By default, reply messages carry a correlation identifier that allows to
 * associate the reply with the preceding request and a domain-specific sender identifier that identifies the
 * sender with respect to some aspect. The body of the message can be of an arbitrary type that inherits
 * from {@link DomainMessageBody}.
 *
 * @param <T> The type of the message body
 */
public class ReplyMessage<T extends DomainMessageBody> extends DomainMessage<T> {

    //User-defined correlation identifier
    private String correlationId;

    //Domain-specific sender name
    private String senderName;

    /**
     * Creates a new reply message without data and content.
     */
    public ReplyMessage() {
        super();
    }

    /**
     * Creates a new reply message from a given message body.
     *
     * @param messageBody The message body to use
     */
    public ReplyMessage(T messageBody) {
        super(messageBody);
    }

    /**
     * Returns the correlation identifier which allows to associate this reply with the corresponding request message.
     *
     * @return The correlation identifier
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the correlation identifier which allows to associate this reply with the corresponding request message.
     *
     * @param correlationId The correlation identifier to set
     * @return The reply message
     */
    public ReplyMessage<T> setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }

    /**
     * Sets the sender name which allows to identify the sender of this reply message based on domain-specific
     * information.
     *
     * @return The sender name
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * Sets the sender name which allows to identify the sender of this reply message based on domain-specific
     * information.
     *
     * @param senderName The sender name to set
     * @return The reply message
     */
    public ReplyMessage<T> setSenderName(String senderName) {
        this.senderName = senderName;
        return this;
    }
}
