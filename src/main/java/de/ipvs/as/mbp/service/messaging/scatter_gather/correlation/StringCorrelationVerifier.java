package de.ipvs.as.mbp.service.messaging.scatter_gather.correlation;

import de.ipvs.as.mbp.service.messaging.scatter_gather.RequestStageConfig;

/**
 * Verifies whether a received string message correlates with a {@link RequestStageConfig} that generated
 * the request for which the message serves as a potential reply. A correlation exists, when the message is actually
 * related to the configuration and can be considered as valid reply for it. Correlation verification is necessary
 * in order to avoid duplicated messages when multiple scatter gather requests run in parallel and make use of a common reply
 * topic.
 */
public interface StringCorrelationVerifier extends CorrelationVerifier<String> {

}
