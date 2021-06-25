package de.ipvs.as.mbp.service.messaging.scatter_gather.correlation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.scatter_gather.RequestStageConfig;
import de.ipvs.as.mbp.util.Json;

/**
 * Verifies whether a received domain message correlates with a {@link RequestStageConfig} that generated
 * the request for which the message serves as a potential reply. A correlation exists, when the message is actually
 * related to the configuration and can be considered as valid reply for it. Correlation verification is necessary
 * in order to avoid duplicated messages when multiple scatter gather requests run in parallel and make use of a common reply
 * topic.
 *
 * @param <T> The domain message type to which the incoming messages are supposed to be transformed
 */
public class DomainCorrelationVerifier<T extends DomainMessage<? extends DomainMessageBody>> implements StringCorrelationVerifier {

    /**
     * Correlation verification interface that has to be implemented by objects of this class. It allows to verify
     * whether a received domain message correlates with a {@link RequestStageConfig} that generated
     * the request for which the message serves as a potential reply.
     *
     * @param <T>
     */
    public interface IDomainCorrelationVerifier<T extends DomainMessage<? extends DomainMessageBody>> extends CorrelationVerifier<T> {

    }

    //Fields
    private final TypeReference<T> typeReference;
    private final DomainCorrelationVerifier.IDomainCorrelationVerifier<T> correlationVerifier;

    /**
     * Creates a new domain message correlation verifier from a given type references, which describes the type to
     * which the message is supposed to be transformed, and a given correlation verifier which is supposed to
     * verify the correlation between the transformed message and the corresponding {@link RequestStageConfig}.
     *
     * @param typeReference       The type reference to use
     * @param correlationVerifier The correlation verifier to use
     */
    public DomainCorrelationVerifier(TypeReference<T> typeReference, DomainCorrelationVerifier.IDomainCorrelationVerifier<T> correlationVerifier) {
        //Set fields
        this.typeReference = typeReference;
        this.correlationVerifier = correlationVerifier;
    }


    /**
     * Returns whether the given message is correlated to the given scatter gather request stage configuration.
     *
     * @param message The message to check
     * @param config  The request stage configuration to check
     * @return True, if the message and the configuration are correlated; false otherwise
     */
    @Override
    public boolean isCorrelated(String message, RequestStageConfig config) {
        try {
            //Transform message to domain message object of provided type
            T domainMessage = Json.MAPPER.readValue(message, this.typeReference);

            //Call provided correlation verifier to do the verification
            return this.correlationVerifier.isCorrelated(domainMessage, config);
        } catch (JsonProcessingException e) {
            System.err.printf("Failed to create domain message from JSON string: %s%n", e.getMessage());
        }
        return false;
    }
}
