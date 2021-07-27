package de.ipvs.as.mbp.domain.discovery.deployment.log;

/**
 * Enumeration of agents that may trigger events leading to the creation of {@link DiscoveryLogEntry}s.
 */
public enum DiscoveryLogEntryTrigger {
    UNKNOWN, //Trigger is unknown
    MBP, //The MBP application personally
    DISCOVERY_REPOSITORY, //An event of a discovery repository
    USER; //The user
}
