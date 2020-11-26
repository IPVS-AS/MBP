package org.citopt.connde.domain.access_control.dto;

import java.util.ArrayList;
import java.util.List;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACAbstractEffect;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.hateoas.server.core.Relation;

/**
 * DTO used for responses that contain {@link ACPolicy policies}.
 * 
 * @author Jakob Benz
 */
@Relation(collectionRelation = "policies", itemRelation = "policies")
public class ACPolicyResponseDTO {
	
	private String id;
	
	private String name;
	
	private String description;
	
	private List<ACAccessType> accessTypes = new ArrayList<>();
	
	private ACAbstractCondition condition;
	
	private ACAbstractEffect effect;
	
	// - - -
	
	public ACPolicyResponseDTO() {}
	
	public ACPolicyResponseDTO(String id, String name, String description, List<ACAccessType> accessTypes, ACAbstractCondition condition, ACAbstractEffect effect) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.accessTypes = accessTypes;
		this.condition = condition;
		this.effect = effect;
	}
	
	// - - -
	
	public String getId() {
		return id;
	}
	
	public ACPolicyResponseDTO setId(String id) {
		this.id = id;
		return this;
	}

	public String getName() {
		return name;
	}
	
	public ACPolicyResponseDTO setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ACPolicyResponseDTO setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public List<ACAccessType> getAccessTypes() {
		return accessTypes;
	}
	
	public ACPolicyResponseDTO setAccessTypes(List<ACAccessType> accessTypes) {
		this.accessTypes = accessTypes;
		return this;
	}
	
	public ACAbstractCondition getCondition() {
		return condition;
	}
	
	public ACPolicyResponseDTO setCondition(ACAbstractCondition condition) {
		this.condition = condition;
		return this;
	}
	
	public ACAbstractEffect getEffect() {
		return effect;
	}
	
	public ACPolicyResponseDTO setEffect(ACAbstractEffect effect) {
		this.effect = effect;
		return this;
	}
	
}
