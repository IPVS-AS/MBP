package de.ipvs.as.mbp.domain.discovery.deployment.log;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.util.InstantToEpochMilliSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;

/**
 * Objects of this class represent the individual log messages that are collected within
 * {@link DiscoveryLog}s.
 */
public class DiscoveryLogMessage {

    @ApiModelProperty(notes = "Timestamp when the log message was created.")
    @JsonSerialize(using = InstantToEpochMilliSerializer.class)
    private Instant time;

    @ApiModelProperty("The type of the log message, indicating its relevance.")
    private DiscoveryLogMessageType type;

    @ApiModelProperty("The actual message.")
    private String message;

    /**
     * Creates a new, empty {@link DiscoveryLogMessage}.
     */
    public DiscoveryLogMessage() {
        //Set timestamp to current time
        setTime(Instant.now());
    }

    /**
     * Creates a new {@link DiscoveryLogMessage} from a given {@link DiscoveryLogMessageType}, indicating the relevance
     * of the {@link DiscoveryLogMessage}, and the actual message string.
     *
     * @param type    The type  to use
     * @param message The actual message string to use
     */
    public DiscoveryLogMessage(DiscoveryLogMessageType type, String message) {
        //Call default constructor
        this();

        //Set fields
        setType(type);
        setMessage(message);
    }

    /**
     * Returns the timestamp when the {@link DiscoveryLogMessage} was created.
     *
     * @return The timestamp
     */
    public Instant getTime() {
        return time;
    }

    /**
     * Sets the timestamp when the {@link DiscoveryLogMessage} was created.
     *
     * @param time The timestamp to set
     * @return The {@link DiscoveryLogMessage}
     */
    public DiscoveryLogMessage setTime(Instant time) {
        this.time = time;
        return this;
    }

    /**
     * Returns the type of the {@link DiscoveryLogMessage}, indicating its relevance.
     *
     * @return The type
     */
    public DiscoveryLogMessageType getType() {
        return type;
    }

    /**
     * Sets the type of the {@link DiscoveryLogMessage}, indicating its relevance.
     *
     * @param type The type to set
     * @return The {@link DiscoveryLogMessage}
     */
    public DiscoveryLogMessage setType(DiscoveryLogMessageType type) {
        this.type = type;
        return this;
    }

    /**
     * Returns the actual message string of the {@link DiscoveryLogMessage}.
     *
     * @return The message string
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the actual message string of the {@link DiscoveryLogMessage}.
     *
     * @param message The message string to set
     * @return The {@link DiscoveryLogMessage}
     */
    public DiscoveryLogMessage setMessage(String message) {
        this.message = message;
        return this;
    }
}
