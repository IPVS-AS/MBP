package de.ipvs.as.mbp.domain.discovery.deployment.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of agents that may trigger events leading to the creation of {@link DiscoveryLog}s.
 */
public enum DiscoveryLogTrigger {
    UNKNOWN("Unknown"), //Trigger is unknown
    MBP("MBP"), //The MBP application personally
    DISCOVERY_REPOSITORY("Discovery Repository"), //An event of a discovery repository
    USER("User"); //The user

    //Externally visible name of the trigger
    private String name;

    /**
     * Creates a new {@link DiscoveryLogTrigger} from a given name.
     *
     * @param name The name to use
     */
    DiscoveryLogTrigger(String name) {
        setName(name);
    }

    /**
     * Sets the name of the {@link DiscoveryLogTrigger}.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if ((name == null) || name.isEmpty()) throw new IllegalArgumentException("The name must not be null or empty.");

        this.name = name;
    }

    /**
     * Serializes the {@link DiscoveryLogTrigger} by returning its name.
     *
     * @return The name of the trigger
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the {@link DiscoveryLogTrigger} that corresponds to a given name. This method is called when
     * a provided trigger name needs to be mapped to the actual trigger object.
     *
     * @param name The name of the {@link DiscoveryLogTrigger}
     * @return The corresponding {@link DiscoveryLogTrigger} or null if not found
     */
    @JsonCreator
    public static DiscoveryLogTrigger create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available trigger against the provided name
        for (DiscoveryLogTrigger trigger : values()) {
            if (name.equalsIgnoreCase(trigger.value())) {
                //Matching trigger found
                return trigger;
            }
        }

        //No matching trigger was found
        return null;
    }
}
