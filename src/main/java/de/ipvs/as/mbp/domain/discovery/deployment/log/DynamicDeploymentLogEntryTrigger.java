package de.ipvs.as.mbp.domain.discovery.deployment.log;

/**
 * Enumeration of possible triggers for events that lead to the creation of a {@link DynamicDeploymentLogEntry}.
 */
public enum DynamicDeploymentLogEntryTrigger {
    UNKNOWN, //Trigger is unknown
    MBP, //The MBP application personally
    DISCOVERY_REPOSITORY, //An event of a discovery repository
    USER; //The user
}
