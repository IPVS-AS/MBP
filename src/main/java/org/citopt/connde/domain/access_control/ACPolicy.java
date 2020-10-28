package org.citopt.connde.domain.access_control;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.domain.user.User;
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
	 * The id of the {@link ACAbstractCondition} of this policy.
	 */
	@Nonnull
	private String conditionId;
	
	/**
	 * The id of the {@link ACAbstractEffect} to apply in case
	 * <ol>
	 *   <li>this policy is specified for {@link ACAccess} of type {@link ACAccessType#READ}, and</li>
	 *   <li>the condition of this policy evaluates to {@code true}.</li>
	 * </ol>
	 * Note that this list can be empty.
	 */
	private String effectId;
	
	// NOTE: DBRefs are not used here, since Spring Data MongoDB lacks the support for DBRefs in retrieving queries.
	//       Furthermore, even the MongoDB guys themselves do not recommend using DBRefs (e.g., see https://jira.spring.io/browse/DATAMONGO-1584)
	//       since DBRefs basically are joins and apparently MongoDB isn't very good at that. Instead we simply store the id of the embedded
	//       documents and do the lookup on the application level. From a performance / overhead point of view it's basically the same,
	//       since we now have to do the lookup when retrieving entities but not when persisting (creating) them.
	
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
	 * @param conditionId the id of the {@link ACAbstractCondition} of this policy.
	 * @param effectId the id of the {@link ACAbstractEffect} to apply if required.
	 * @param ownerId the id of the {@link User} that owns this policy.
	 */
	public ACPolicy(String name, String description, List<ACAccessType> accessTypes, String conditionId, String effectId, String ownerId) {
		super(name, description, ownerId);
		this.accessTypes = accessTypes;
		this.conditionId = conditionId;
		this.effectId = effectId;
	}
	
	// - - -
	
	public List<ACAccessType> getAccessTypes() {
		return accessTypes;
	}
	
	public ACPolicy setAccessTypes(List<ACAccessType> accessTypes) {
		this.accessTypes = accessTypes;
		return this;
	}

	public String getConditionId() {
		return conditionId;
	}

	public ACPolicy setConditionId(String conditionId) {
		this.conditionId = conditionId;
		return this;
	}

	public String getEffectId() {
		return effectId;
	}

	public ACPolicy setEffectId(String effectId) {
		this.effectId = effectId;
		return this;
	}
	
}
