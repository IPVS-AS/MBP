package de.ipvs.as.mbp.domain.discovery.deployment.log;

import io.swagger.annotations.ApiModelProperty;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Objects of this class represent log entries within {@link DiscoveryLog}s, collecting various individual
 * {@link DiscoveryLogMessage}s for a certain task.
 */
public class DiscoveryLogEntry {

    @ApiModelProperty("The agent that triggered the event leading to the creation of the log entry.")
    private DiscoveryLogEntryTrigger trigger;

    @ApiModelProperty("The name of the task for which the log messages are collected in this entry.")
    private String taskName;

    @ApiModelProperty("The individual messages of log entry.")
    private List<DiscoveryLogMessage> messages;

    /**
     * Creates a new, empty {@link DiscoveryLogEntry}.
     */
    public DiscoveryLogEntry() {
        //Initialize data structures
        this.messages = new LinkedList<>();
    }

    /**
     * Creates a new {@link DiscoveryLogEntry} from a given {@link DiscoveryLogEntryTrigger} and task name.
     *
     * @param trigger  The agent that triggered the event leading to the creation of the {@link DiscoveryLogEntry}
     * @param taskName The name of the task for which the log messages are collected
     */
    public DiscoveryLogEntry(DiscoveryLogEntryTrigger trigger, String taskName) {
        //Call default constructor
        this();

        //Set fields
        setTrigger(trigger);
        setTaskName(taskName);
    }

    /**
     * Creates a new {@link DiscoveryLogEntry} from a given {@link DiscoveryLogEntryTrigger}, a task name and a
     * {@link Collection} of the actual {@link DiscoveryLogMessage}s.
     *
     * @param trigger  The agent that triggered the event leading to the creation of the {@link DiscoveryLogEntry}
     * @param taskName The name of the task for which the log messages are collected
     * @param messages The actual {@link DiscoveryLogMessage}s
     */
    public DiscoveryLogEntry(DiscoveryLogEntryTrigger trigger, String taskName, Collection<DiscoveryLogMessage> messages) {
        //Call default constructor
        this();

        //Set fields
        setTrigger(trigger);
        setTaskName(taskName);
        setMessages(messages);
    }

    /**
     * Returns the trigger of the {@link DiscoveryLogEntry}.
     *
     * @return The trigger
     */
    public DiscoveryLogEntryTrigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger of the {@link DiscoveryLogEntry}.
     *
     * @param trigger The trigger to set
     * @return The {@link DiscoveryLogEntry}
     */
    private DiscoveryLogEntry setTrigger(DiscoveryLogEntryTrigger trigger) {
        //Null check
        if (trigger == null) throw new IllegalArgumentException("The trigger must not be null.");

        this.trigger = trigger;
        return this;
    }

    /**
     * Returns the name of the task for which the {@link DiscoveryLogMessage}s are collected in this entry.
     *
     * @return The task name
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Sets the name of the task for which the {@link DiscoveryLogMessage}s are collected in this entry
     *
     * @param taskName The task name to set
     * @return The {@link DiscoveryLogEntry}
     */
    private DiscoveryLogEntry setTaskName(String taskName) {
        //Null check
        if (taskName == null) taskName = "";

        this.taskName = taskName;
        return this;
    }

    /**
     * Returns the individual {@link DiscoveryLogMessage}s of the {@link DiscoveryLogEntry} as list.
     *
     * @return The list of {@link DiscoveryLogMessage}s
     */
    public List<DiscoveryLogMessage> getMessages() {
        return messages;
    }

    /**
     * Sets the individual messages of the {@link DiscoveryLogEntry}.
     *
     * @param messages The {@link Collection} of {@link DiscoveryLogMessage}s to set
     * @return The {@link DiscoveryLogEntry}
     */
    public DiscoveryLogEntry setMessages(Collection<DiscoveryLogMessage> messages) {
        //Null checks
        if ((messages == null) || messages.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The messages must not be null.");
        }

        //Transform collection to list
        this.messages = new LinkedList<>(messages);
        return this;
    }

    /**
     * Adds a given {@link DiscoveryLogMessage} to the {@link DiscoveryLogEntry}.
     *
     * @param message The message to add
     * @return The {@link DiscoveryLogEntry}
     */
    public DiscoveryLogEntry addMessage(DiscoveryLogMessage message) {
        //Null check
        if (message == null) throw new IllegalArgumentException("The message must not be null.");

        //Add the message
        this.messages.add(message);
        return this;
    }

    /**
     * Returns whether the {@link DiscoveryLogEntry} is empty, i.e. contains no {@link DiscoveryLogMessage}s.
     *
     * @return True, if no {@link DiscoveryLogMessage}s are contained; false otherwise
     */
    public boolean isEmpty() {
        return this.messages.isEmpty();
    }
}
