package de.ipvs.as.mbp.service.messaging.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.settings.SettingsService;

/**
 * Objects of this class represent request messages, i.e. messages that contain data and intent to invoke
 * a specific behaviour of the receiver as reaction to receiving this message, which typically involves the sending
 * of a corresponding {@link ReplyMessage} to the primal sender. By default, request messages carry a return topic
 * that describes the topic under which the reply messages for each request are expected to be published and
 * a correlation identifier that allows to associate replies with the preceding requests. The body of the message
 * can be of an arbitrary type that inherits from {@link DomainMessageBody}.
 *
 * @param <T> The type of the message body
 */
public class RequestMessage<T extends DomainMessageBody> extends DomainMessage<T> {
    //Topic for reply messages
    private String returnTopic;

    //User-defined correlation identifier
    private String correlationId;

    /**
     * Creates a new request message from a given message body.
     *
     * @param messageBody The message body to use
     */
    public RequestMessage(T messageBody) {
        super(messageBody);
    }

    /**
     * Creates a new request message from a given message body and return topic.
     *
     * @param messageBody The message body to use
     * @param returnTopic The return topic to use
     */
    public RequestMessage(T messageBody, String returnTopic) {
        super(messageBody);
        setReturnTopic(returnTopic);
    }

    /**
     * Creates a new request message as copy of a existing request message. However, no copy of the message body is
     * created; instead the same reference is used.
     *
     * @param requestMessage The existing request message to copy
     */
    public RequestMessage(RequestMessage<T> requestMessage) {
        //Call copy constructor of super class
        super(requestMessage);

        //Copy fields from the provided request message
        this.returnTopic = requestMessage.returnTopic;
        this.correlationId = requestMessage.correlationId;
    }

    /**
     * Returns the sender name that is displayed in messages which are published by the MBP in order to make the
     * MBP identifiable. The sender name is directly retrieved from the settings by using the {@link SettingsService}.
     *
     * @return The retrieved sender name
     */
    @JsonProperty
    public String getSenderName() {
        //Get SettingsService through the DynamicBeanProvider and retrieve the sender name
        return DynamicBeanProvider.get(SettingsService.class).getSettings().getSenderName();
    }

    /**
     * Returns the return topic of the request message which indicates under which topic potential reply messages
     * to this request are expected to be published.
     *
     * @return The return topic
     */
    public String getReturnTopic() {
        return returnTopic;
    }

    /**
     * Sets the return topic of the request message which indicates under which topic potential reply messages
     * to this request are expected to be published.
     *
     * @param returnTopic The return topic to set
     * @return The request message
     */
    public RequestMessage<T> setReturnTopic(String returnTopic) {
        this.returnTopic = returnTopic;
        return this;
    }


    /**
     * Returns the correlation identifier which allows to associate potential reply messages to this request.
     *
     * @return The correlation identifier
     */
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Returns the correlation identifier which allows to associate potential reply messages to this request.
     *
     * @param correlationId The correlation identifier to set
     * @return The request message
     */
    public RequestMessage<T> setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
        return this;
    }
}
