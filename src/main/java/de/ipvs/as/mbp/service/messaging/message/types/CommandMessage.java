package de.ipvs.as.mbp.service.messaging.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.settings.SettingsService;

/**
 * Objects of this class represent general command messages, i.e. messages that contain data and intent to invoke
 * a specific behaviour of the receiver as reaction to receiving this message, but typically without the sending
 * of a corresponding {@link ReplyMessage} to the primal sender. The body of the message can be of an arbitrary type
 * that inherits from {@link DomainMessageBody}.
 *
 * @param <T> The type of the message body
 */
public class CommandMessage<T extends DomainMessageBody> extends DomainMessage<T> {
    /**
     * Creates a new request message from a given message body.
     *
     * @param messageBody The message body to use
     */
    public CommandMessage(T messageBody) {
        super(messageBody);
    }

    /**
     * Creates a new command message from a given message body.
     *
     * @param messageBody The message body to use
     */
    public CommandMessage(T messageBody, String returnTopic) {
        super(messageBody);
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
}
