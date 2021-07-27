package de.ipvs.as.mbp.domain.discovery.deployment.log;

/**
 * Enumeration of possible types of {@link DiscoveryLogMessage}s, indicating their relevance.
 */
public enum DiscoveryLogMessageType {
    INFO, //General information
    SUCCESS, //Action succeeded
    UNDESIRABLE, //Occurrence of undesirable events
    ERROR; //Error hindering certain actions
}
