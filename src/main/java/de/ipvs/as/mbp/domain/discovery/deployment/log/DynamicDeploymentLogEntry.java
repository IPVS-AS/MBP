package de.ipvs.as.mbp.domain.discovery.deployment.log;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.util.InstantToEpochMilliSerializer;
import io.swagger.annotations.ApiModelProperty;

import java.time.Instant;

/**
 * Objects of this class represent log entries within {@link DynamicDeploymentLog}s.
 */
public class DynamicDeploymentLogEntry {
    @ApiModelProperty(notes = "Timestamp when the log was written.")
    @JsonSerialize(using = InstantToEpochMilliSerializer.class)
    private Instant time;

    @ApiModelProperty("The type of the log entry, indicating its relevance.")
    private DynamicDeploymentLogEntryType type;

    @ApiModelProperty("The trigger that lead to the event which is described by the log entry.")
    private DynamicDeploymentLogEntryTrigger trigger;

    @ApiModelProperty("The name of the task that issued the writing of the log entry (if applicable).")
    private String taskName;

    @ApiModelProperty("The actual message of the log entry.")
    private String message;

    /**
     * Creates a new, empty {@link DynamicDeploymentLogEntry}.
     */
    public DynamicDeploymentLogEntry() {
        //Set timestamp to now
        setTime(Instant.now());
    }

    /**
     * Creates a new {@link DynamicDeploymentLogEntryType} from a given {@link DynamicDeploymentLogEntryType}, a
     * {@link DynamicDeploymentLogEntryTrigger}, a task name and a message.
     *
     * @param type     The type of the log entry
     * @param trigger  The trigger that lead to the described event
     * @param taskName The name of the task writing the log entry
     * @param message  The actual log message
     */
    public DynamicDeploymentLogEntry(DynamicDeploymentLogEntryType type, DynamicDeploymentLogEntryTrigger trigger, String taskName, String message) {
        //Call default constructor
        this();

        //Set fields
        setType(type);
        setTrigger(trigger);
        setTaskName(taskName);
        setMessage(message);
    }

    /**
     * Returns the timestamp when the log was written.
     *
     * @return The timestamp
     */
    public Instant getTime() {
        return time;
    }

    /**
     * Sets the timestamp when the log was written.
     *
     * @param time The timestamp to set
     * @return The log entry
     */
    public DynamicDeploymentLogEntry setTime(Instant time) {
        //Null check
        if(time == null) throw new IllegalArgumentException("The timestamp must not be null.");

        this.time = time;
        return this;
    }

    /**
     * Returns the type of the log entry, indicating its relevance.
     *
     * @return The type
     */
    public DynamicDeploymentLogEntryType getType() {
        return type;
    }

    /**
     * Sets the type of the log entry, indicating its relevance.
     *
     * @param type The type to set
     * @return The log entry
     */
    public DynamicDeploymentLogEntry setType(DynamicDeploymentLogEntryType type) {
        //Null check
        if (type == null) throw new IllegalArgumentException("The type must not be null.");

        this.type = type;
        return this;
    }

    /**
     * Returns the trigger that lead to the event which is described by the log entry.
     *
     * @return The trigger
     */
    public DynamicDeploymentLogEntryTrigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger that lead to the event which is described by the log entry.
     *
     * @param trigger The trigger to set
     * @return The log entry
     */
    public DynamicDeploymentLogEntry setTrigger(DynamicDeploymentLogEntryTrigger trigger) {
        //Null check
        if (trigger == null) throw new IllegalArgumentException("The trigger must not be null.");

        this.trigger = trigger;
        return this;
    }

    /**
     * Returns the name of the task that issued the writing of the log entry (if applicable).
     *
     * @return The task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets the name of the task that issued the writing of the log entry (if applicable).
     *
     * @param taskName The task name to set
     * @return The log entry
     */
    public DynamicDeploymentLogEntry setTaskName(String taskName) {
        //Null check
        if (taskName == null) taskName = "";

        this.taskName = taskName;
        return this;
    }

    /**
     * Returns the actual message of the log entry.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the actual message of the log entry.
     *
     * @param message The message to set
     * @return The log entry
     */
    public DynamicDeploymentLogEntry setMessage(String message) {
        //Sanity check
        if ((message == null) || message.isEmpty()) {
            throw new IllegalArgumentException("The message must not be null or empty.");
        }

        this.message = message;
        return this;
    }
}
