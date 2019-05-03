package org.citopt.connde.domain.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Date;

/**
 * Objects of this class represent rules that consist out of a trigger and an action. These rules are then managed
 * by a dedicated service which controls the execution: If the trigger of a rule is fired, its action will be
 * executed subsequently by this service. In addition, the rule objects hold the date of the last execution and the
 * total number of executions that were performed.
 */
@Document
public class Rule {
    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    @DBRef
    private RuleTrigger trigger;

    @DBRef
    private RuleAction action;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date lastExecution;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private int executions;

    /**
     * Returns the id of the rule.
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the rule.
     *
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the rule.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the rule.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the trigger of the rule.
     *
     * @return The trigger
     */
    public RuleTrigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the trigger of the rule.
     *
     * @param trigger The trigger to set
     */
    public void setTrigger(RuleTrigger trigger) {
        this.trigger = trigger;
    }

    /**
     * Returns the action of the rule.
     *
     * @return The action
     */
    public RuleAction getAction() {
        return action;
    }

    /**
     * Sets the action of the rule.
     *
     * @param action The action to set
     */
    public void setAction(RuleAction action) {
        this.action = action;
    }

    /**
     * Returns the date of the last execution of the rule.
     *
     * @return The date
     */
    public Date getLastExecution() {
        return lastExecution;
    }

    /**
     * Sets the date of the last execution of the rule.
     *
     * @param lastExecution The date to set
     */
    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    /**
     * Sets the date of the last execution of the rule to the current date.
     */
    public void setLastExecutionToNow() {
        this.lastExecution = new Date();
    }

    /**
     * Returns the total number of executions of the rule.
     *
     * @return The number of executions
     */
    public int getExecutions() {
        return executions;
    }

    /**
     * Sets the total number of executions of the rule.
     *
     * @param executions The number of executions to set
     */
    public void setExecutions(int executions) {
        this.executions = executions;
    }

    /**
     * Increases the total number of executions of the rule by one.
     */
    public void increaseExecutions() {
        this.executions++;
    }
}
