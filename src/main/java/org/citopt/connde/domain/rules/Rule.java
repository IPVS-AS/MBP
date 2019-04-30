package org.citopt.connde.domain.rules;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Date;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RuleTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(RuleTrigger trigger) {
        this.trigger = trigger;
    }

    public RuleAction getAction() {
        return action;
    }

    public void setAction(RuleAction action) {
        this.action = action;
    }

    public Date getLastExecution() {
        return lastExecution;
    }

    public void setLastExecution(Date lastExecution) {
        this.lastExecution = lastExecution;
    }

    public int getExecutions() {
        return executions;
    }

    public void setExecutions(int executions) {
        this.executions = executions;
    }
}
