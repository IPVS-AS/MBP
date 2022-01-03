package de.ipvs.as.mbp.service.messaging.message.types;

import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;

/**
 * Objects of this class represent document messages, i.e. messages that can be used to transfer data and that do not
 * request a specific behaviour of the receiver as reaction to receiving this message. The body of the message can be
 * of an arbitrary type that inherits from {@link DomainMessageBody}.
 *
 * @param <T> The type of the message body
 */
public class DocumentMessage<T extends DomainMessageBody> extends DomainMessage<T> {
    /**
     * Creates a new document message from a given message body.
     *
     * @param messageBody The message body to use
     */
    public DocumentMessage(T messageBody) {
        super(messageBody);
    }
}
