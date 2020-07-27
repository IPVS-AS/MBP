package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.GeneratedValue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import io.swagger.annotations.ApiModel;

/**
 * A policy that is defined for an {@link ACAccess} and is evaluated
 * in order to determine whether to grant or deny the requested access
 * and whether to apply certain constraints / effects to the data
 * presented to the requesting entity.
 * 
 * @author Jakob Benz
 */
@Document
@ApiModel("Model for Access Control Policies.")
public class ACPolicy<T> {
	
	/**
	 * The id of this policy.
	 */
	@Id
    @GeneratedValue
	private String id;
	
	/**
	 * The name of this policy;
	 */
	private String name;
	
	/**
	 * The priority of this policy. A lower value indicates higher priority.
	 */
	@Nonnull
	@Min(0)
	private int priority;
	
	/**
	 * The list of access {@link ACAccessType types} this policy
	 * is applicable for.
	 */
	@NotEmpty
	private List<ACAccessType> accessTypes = new ArrayList<>();
	
	/**
	 * The {@link IACCondition} of this policy.
	 */
	@Nonnull
	private IACCondition condition;
	
	/**
	 * The list of {@link IACEffect effects} to apply in case
	 * <ol>
	 *   <li>this policy is specified for {@link ACAccess} of type {@link ACAccessType#READ}, and</li>
	 *   <li>the condition of this policy evaluates to {@code true}.</li>
	 * </ol>
	 * Note that this list can be empty.
	 */
	private List<IACEffect<T>> effects = new ArrayList<>();

	// - - -
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this policy.
	 * @param priority the priority of this policy. A lower value indicates higher priority.
	 * @param accessTypes the list of access {@link ACAccessType types} this policy is applicable for.
	 * @param condition the {@link IACCondition} of this policy.
	 * @param effects the list of {@link IACEffect effects} to apply if required.
	 */
	public ACPolicy(String name, int priority, List<ACAccessType> accessTypes, IACCondition condition, List<IACEffect<T>> effects) {
		this.name = name;
		this.priority = priority;
		this.accessTypes = accessTypes;
		this.condition = condition;
		this.effects = effects;
	}
	
	// - - -
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public ACPolicy<T> setName(String name) {
		this.name = name;
		return this;
	}

	public int getPriority() {
		return priority;
	}

	public ACPolicy<T> setPriority(int priority) {
		this.priority = priority;
		return this;
	}
	
	public List<ACAccessType> getAccessTypes() {
		return accessTypes;
	}
	
	public ACPolicy<T> setAccessTypes(List<ACAccessType> accessTypes) {
		this.accessTypes = accessTypes;
		return this;
	}

	public IACCondition getCondition() {
		return condition;
	}

	public ACPolicy<T> setCondition(IACCondition condition) {
		this.condition = condition;
		return this;
	}

	public List<IACEffect<T>> getEffects() {
		return effects;
	}

	public ACPolicy<T> setEffects(List<IACEffect<T>> effects) {
		this.effects = effects;
		return this;
	}
	
}
