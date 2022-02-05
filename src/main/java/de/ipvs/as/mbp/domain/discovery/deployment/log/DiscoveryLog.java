package de.ipvs.as.mbp.domain.discovery.deployment.log;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.util.InstantToEpochMilliSerializer;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Objects of this class represent discovery logs of {@link DynamicDeployment}s that collect and group the various
 * {@link DiscoveryLogMessage}s that are created and recorded during the execution of a certain task.
 */
@Document
public class DiscoveryLog {

    @ApiModelProperty("The generated ID of the disovery log.")
    @Id
    @GeneratedValue
    private String id;

    @ApiModelProperty("The ID of the dynamic deployment to which this log belongs.")
    private String dynamicDeploymentId;

    @ApiModelProperty("The timestamp when the associated task started.")
    @JsonSerialize(using = InstantToEpochMilliSerializer.class)
    private Instant startTime;

    @ApiModelProperty("The timestamp when the associated task terminated.")
    @JsonSerialize(using = InstantToEpochMilliSerializer.class)
    private Instant endTime;

    @ApiModelProperty("The agent that triggered the event leading to the creation of this log.")
    private DiscoveryLogTrigger trigger;

    @ApiModelProperty("The name of the task for which the log messages are collected in this log.")
    private String taskName;

    @ApiModelProperty("The individual messages of the log.")
    private List<DiscoveryLogMessage> messages;

    /**
     * Creates a new, empty {@link DiscoveryLog}.
     */
    public DiscoveryLog() {
        //Initialize data structures
        this.messages = new LinkedList<>();

        //Update timestamps
        updateStartTimestamp();
        updateEndTimestamp();
    }

    /**
     * Creates a new {@link DiscoveryLog} from a given {@link DiscoveryLogTrigger} and a task name.
     *
     * @param trigger  The agent that triggered the event leading to the creation of the {@link DiscoveryLog}
     * @param taskName The name of the task for which the log messages are collected
     */
    public DiscoveryLog(DiscoveryLogTrigger trigger, String taskName) {
        //Call default constructor
        this();

        //Set fields
        setTrigger(trigger);
        setTaskName(taskName);
    }

    /**
     * Creates a new {@link DiscoveryLog} from a given {@link DiscoveryLogTrigger}, a task name and a
     * {@link Collection} of the actual {@link DiscoveryLogMessage}s.
     *
     * @param trigger  The agent that triggered the event leading to the creation of the {@link DiscoveryLog}
     * @param taskName The name of the task for which the log messages are collected
     * @param messages The actual {@link DiscoveryLogMessage}s
     */
    public DiscoveryLog(DiscoveryLogTrigger trigger, String taskName, Collection<DiscoveryLogMessage> messages) {
        //Call default constructor
        this();

        //Set fields
        setTrigger(trigger);
        setTaskName(taskName);
        setMessages(messages);
    }

    /**
     * Returns the ID of the {@link DiscoveryLog}.
     *
     * @return The ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the {@link DiscoveryLog}.
     *
     * @param id The ID to set
     * @return The {@link DiscoveryLog}
     */
    private DiscoveryLog setId(String id) {
        //Sanity check
        if ((id == null) || id.isEmpty()) throw new IllegalArgumentException("The ID must not be null or empty.");

        this.id = id;
        return this;
    }

    /**
     * Returns the ID of the {@link DynamicDeployment} to which this log belongs.
     *
     * @return The ID of the {@link DynamicDeployment}
     */
    public String getDynamicDeploymentId() {
        return dynamicDeploymentId;
    }

    /**
     * Sets the ID of the {@link DynamicDeployment} to which this log belongs.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to set
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog setDynamicDeploymentId(String dynamicDeploymentId) {
        this.dynamicDeploymentId = dynamicDeploymentId;
        return this;
    }

    /**
     * Returns the timestamp when the task, for which the {@link DiscoveryLogMessage}s are collected, started.
     *
     * @return The start timestamp
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Sets the timestamp when the task, for which the {@link DiscoveryLogMessage}s are collected, started.
     *
     * @param startTime The start timestamp to set
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Updates the start timestamp, which indicates when the associated task started, to the current time.
     *
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog updateStartTimestamp() {
        this.startTime = Instant.now();
        return this;
    }

    /**
     * Returns the timestamp when the task, for which the {@link DiscoveryLogMessage}s are collected, terminated.
     *
     * @return The end timestamp
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Sets the timestamp when the task, for which the {@link DiscoveryLogMessage}s are collected, terminated.
     *
     * @param endTime The end timestamp to set
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog setEndTime(Instant endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Updates the start timestamp, which indicates when the associated task terminated, to the current time.
     *
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog updateEndTimestamp() {
        this.endTime = Instant.now();
        return this;
    }

    /**
     * Returns the trigger of the {@link DiscoveryLog}.
     *
     * @return The trigger
     */
    public DiscoveryLogTrigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger of the {@link DiscoveryLog}.
     *
     * @param trigger The trigger to set
     * @return The {@link DiscoveryLog}
     */
    private DiscoveryLog setTrigger(DiscoveryLogTrigger trigger) {
        //Null check
        if (trigger == null) throw new IllegalArgumentException("The trigger must not be null.");

        this.trigger = trigger;
        return this;
    }

    /**
     * Returns the name of the task for which the {@link DiscoveryLogMessage}s are collected in this log.
     *
     * @return The task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets the name of the task for which the {@link DiscoveryLogMessage}s are collected in this log.
     *
     * @param taskName The task name to set
     * @return The {@link DiscoveryLog}
     */
    private DiscoveryLog setTaskName(String taskName) {
        //Null check
        if (taskName == null) taskName = "";

        this.taskName = taskName;
        return this;
    }

    /**
     * Returns the individual {@link DiscoveryLogMessage}s of the {@link DiscoveryLog} as list.
     *
     * @return The list of {@link DiscoveryLogMessage}s
     */
    public List<DiscoveryLogMessage> getMessages() {
        return messages;
    }

    /**
     * Sets the individual messages of the {@link DiscoveryLog}.
     *
     * @param messages The {@link Collection} of {@link DiscoveryLogMessage}s to set
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog setMessages(Collection<DiscoveryLogMessage> messages) {
        //Null checks
        if ((messages == null) || messages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The messages must not be null.");
        }

        //Transform collection to list
        this.messages = new LinkedList<>(messages);
        return this;
    }

    /**
     * Adds a given {@link DiscoveryLogMessage} to the {@link DiscoveryLog}.
     *
     * @param message The message to add
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog addMessage(DiscoveryLogMessage message) {
        //Null check
        if (message == null) throw new IllegalArgumentException("The message must not be null.");

        //Add the message
        this.messages.add(message);
        return this;
    }

    /**
     * Returns whether the {@link DiscoveryLog} is empty, i.e. contains no {@link DiscoveryLogMessage}s.
     *
     * @return True, if no {@link DiscoveryLogMessage}s are contained; false otherwise
     */
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }
}
