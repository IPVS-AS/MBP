package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.domain.user.User;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.hateoas.server.core.Relation;

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
@Relation(collectionRelation = "policies", itemRelation = "policies")
@ApiModel("Model for Access Control Policies.")
public class ACPolicy extends ACAbstractEntity {
	
	/**
	 * The list of access {@link ACAccessType types} this policy
	 * is applicable for.
	 */
	@NotEmpty
	private List<ACAccessType> accessTypes = new ArrayList<>();
	
	/**
	 * The {@link ACAbstractCondition} of this policy.
	 */
	@Nonnull
	@DBRef
	private ACAbstractCondition condition;
	
	/**
	 * The {@link ACAbstractEffect} to apply in case
	 * <ol>
	 *   <li>this policy is specified for {@link ACAccess} of type {@link ACAccessType#READ}, and</li>
	 *   <li>the condition of this policy evaluates to {@code true}.</li>
	 * </ol>
	 * Note that this list can be empty.
	 */
	@DBRef
	private ACAbstractEffect effect;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACPolicy() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this policy.
	 * @param description the description of this policy.
	 * @param accessTypes the list of access {@link ACAccessType types} this policy is applicable for.
	 * @param condition the {@link ACAbstractCondition} of this policy.
	 * @param effect the {@link ACAbstractEffect} to apply if required.
	 * @param owner the {@link User} that owns this policy.
	 */
	public ACPolicy(String name, String description, List<ACAccessType> accessTypes, ACAbstractCondition condition, ACAbstractEffect effect, User owner) {
		super(name, description, owner);
		this.accessTypes = accessTypes;
		this.condition = condition;
		this.effect = effect;
	}
	
	// - - -
	
	public List<ACAccessType> getAccessTypes() {
		return accessTypes;
	}
	
	public ACPolicy setAccessTypes(List<ACAccessType> accessTypes) {
		this.accessTypes = accessTypes;
		return this;
	}

	public ACAbstractCondition getCondition() {
		return condition;
	}

	public ACPolicy setCondition(ACAbstractCondition condition) {
		this.condition = condition;
		return this;
	}

	public ACAbstractEffect getEffect() {
		return effect;
	}

	public ACPolicy setEffect(ACAbstractEffect effect) {
		this.effect = effect;
		return this;
	}
	
}
