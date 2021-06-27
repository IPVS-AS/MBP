package de.ipvs.as.mbp.service.messaging.scatter_gather.correlation;

import de.ipvs.as.mbp.service.messaging.scatter_gather.config.RequestStageConfig;

/**
 * Base interface for more concrete correlation verifier interfaces for specific message data types.
 * Correlation verification is necessary in order to avoid duplicated messages when multiple scatter gather requests
 * run in parallel and make use of a common reply topic.
 *
 * @param <T> The type of the received message
 */
public interface CorrelationVerifier<T> {
    /**
     * Returns whether the given message is correlated to the given scatter gather request stage configuration.
     *
     * @param message The message to check
     * @param config  The request stage configuration to check
     * @return True, if the message and the configuration are correlated; false otherwise
     */
    boolean isCorrelated(T message, RequestStageConfig<?> config);
}
