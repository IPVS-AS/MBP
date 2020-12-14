package de.ipvs.as.mbp.domain.rules;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Objects of this class represent rules that consist out of a trigger and a
 * list of actions. These rules are then managed by a dedicated service which
 * controls the execution: If the trigger of a rule is fired, its action will be
 * executed subsequently by this service. In addition, the rule objects hold the
 * date of the last execution and the total number of executions that were performed.
 */
@MBPEntity(createValidator = RuleCreateValidator.class)
public class Rule extends UserEntity {
	
	@Id
	@GeneratedValue
	private String id;

	@Indexed(unique = true)
	private String name;

	@DBRef
	private RuleTrigger trigger;

	@DBRef
	private List<RuleAction> actions;

	@JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private Date lastExecution = null;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private int executions = 0;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private boolean enabled = false;

	@JsonProperty(access = JsonProperty.Access.READ_ONLY)
	private RuleExecutionResult lastExecutionResult = RuleExecutionResult.NONE;

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
	 * Returns the list of actions of the rule.
	 *
	 * @return The actions
	 */
	public List<RuleAction> getActions() {
		return actions;
	}

	/**
	 * Sets the list of actions of the rule.
	 *
	 * @param actions The actions to set
	 */
	public void setActions(List<RuleAction> actions) {
		this.actions = actions;
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

	/**
	 * Returns whether the rule is currently active.
	 *
	 * @return True, if the rule is active; false otherwise
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether the rule is currently active.
	 *
	 * @param enabled True, if the rule is active; false otherwise
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the result of the last rule execution.
	 *
	 * @return The execution result
	 */
	public RuleExecutionResult getLastExecutionResult() {
		return lastExecutionResult;
	}

	/**
	 * Sets the result of the last rule execution.
	 *
	 * @param lastExecutionResult The execution result to set
	 */
	public void setLastExecutionResult(RuleExecutionResult lastExecutionResult) {
		this.lastExecutionResult = lastExecutionResult;
	}

	/**
	 * Returns the name of the trigger of the rule.
	 *
	 * @return The name of the trigger
	 */
	@JsonProperty("triggerName")
	public String getTriggerName() {
		return this.trigger.getName();
	}

	/**
	 * Returns the List of action names of the rule.
	 *
	 * @return The list of action names
	 */
	@JsonProperty("actionNames")
	public List<String> getActionNames() {
		return this.actions.stream().map(RuleAction::getName).collect(Collectors.toList());
	}

	/**
	 * Overrides the equals method by only considering the ids of rules.
	 *
	 * @param o The object to compare
	 * @return True, if both objects are identical (i.e. have the same id); false
	 *         otherwise
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		Rule that = (Rule) o;
		return id.equals(that.id);
	}

	/**
	 * Overrides the hash code method by using the ids of rules.
	 *
	 * @return The hash code of the object
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}
