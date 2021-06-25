package de.ipvs.as.mbp.service.messaging.dispatcher.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.util.Json;

/**
 * Objects of this class take a listener callback method that is triggered when a domain message is delivered by the
 * messaging broker that was published under a topic that matches the topic filter to which the component
 * that makes use of the object subscribed itself to at the message dispatcher of the messaging service.
 *
 * @param <T> The domain message type to which the incoming messages are supposed to be transformed
 */
public class DomainMessageListener<T extends DomainMessage<? extends DomainMessageBody>> implements StringMessageListener {
    /**
     * Callback interface that has to be implemented by objects of this class for receiving callbacks on the arrival
     * of domain messages..
     *
     * @param <T> The domain message type to which the incoming messages are supposed to be transformed
     */
    public interface IDomainMessageListener<T> extends MessageListener<T> {
    }

    //Fields
    private final TypeReference<T> typeReference;
    private final IDomainMessageListener<T> listener;

    /**
     * Creates a new domain message listener from a given type references, which describes the type to which the
     * message is supposed to be transformed, and a given message listener which receives the callback for the
     * transformed message.
     *
     * @param typeReference The type reference to use
     * @param listener      The message listener to use
     */
    public DomainMessageListener(TypeReference<T> typeReference, IDomainMessageListener<T> listener) {
        //Set fields
        this.typeReference = typeReference;
        this.listener = listener;
    }

    /**
     * Called when a string message is received from the messaging broker that was published under a topic that matches
     * the topic filter to which the implementing component subscribed itself.
     *
     * @param message     The received message
     * @param topic       The topic under which the message was published
     * @param topicFilter The topic filter that was used in the corresponding subscription at the message dispatcher
     */
    @Override
    public void onMessageDispatched(String message, String topic, String topicFilter) {
        try {
            //Convert message to domain message object of provided type
            T domainMessage = Json.MAPPER.readValue(message, this.typeReference);

            //Trigger callback of provided listener
            this.listener.onMessageDispatched(domainMessage, topic, topicFilter);
        } catch (JsonProcessingException e) {
            System.err.printf("Failed to create domain message from JSON string: %s%n", e.getMessage());
        }
    }
}
