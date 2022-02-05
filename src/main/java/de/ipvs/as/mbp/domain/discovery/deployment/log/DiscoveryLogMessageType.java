package de.ipvs.as.mbp.domain.discovery.deployment.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of possible types of {@link DiscoveryLogMessage}s, indicating their relevance.
 */
public enum DiscoveryLogMessageType {
    INFO("Info"), //General information
    SUCCESS("Success"), //Action succeeded
    UNDESIRABLE("Undesirable"), //Occurrence of undesirable events
    ERROR("Error"); //Error hindering certain actions

    //Externally visible name of the message type
    private String name;

    /**
     * Creates a new {@link DiscoveryLogMessageType} from a given name.
     *
     * @param name The name to use
     */
    DiscoveryLogMessageType(String name) {
        setName(name);
    }

    /**
     * Sets the name of the {@link DiscoveryLogMessageType}.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if ((name == null) || name.isEmpty()) throw new IllegalArgumentException("The name must not be null or empty.");

        this.name = name;
    }

    /**
     * Serializes the {@link DiscoveryLogMessageType} by returning its name.
     *
     * @return The name of the message type
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the {@link DiscoveryLogMessageType} that corresponds to a given name. This method is called when
     * a provided message type name needs to be mapped to the actual message type object.
     *
     * @param name The name of the {@link DiscoveryLogMessageType}
     * @return The corresponding {@link DiscoveryLogMessageType} or null if not found
     */
    @JsonCreator
    public static DiscoveryLogMessageType create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available message type against the provided name
        for (DiscoveryLogMessageType messageType : values()) {
            if (name.equalsIgnoreCase(messageType.value())) {
                //Matching message type found
                return messageType;
            }
        }

        //No matching message type was found
        return null;
    }
}
