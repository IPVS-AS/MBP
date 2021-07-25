package de.ipvs.as.mbp.domain.discovery.deployment.log;

/**
 * Enumeration of possible types of {@link DynamicDeploymentLogEntry}s, indicating their relevance.
 */
public enum DynamicDeploymentLogEntryType {
    INFO, //General information
    UNDESIRABLE, //Occurrence of undesirable events
    ERROR; //Error hindering certain actions
}
