package org.citopt.connde.domain.access_control.dto;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.user.User;

/**
 * Request DTO for {@link ACPolicy}. Used, e.g., for the request body
 * when creating new policies.
 * 
 * @author Jakob Benz
 */
public class ACPolicyRequestDTO {
	
	@NotEmpty
	private String name;
	
	@Nonnull
	@Min(0)
	private int priority;
	
	@NotEmpty
	private List<ACAccessType> accessTypes = new ArrayList<>();
	
	@Nonnull
	private String conditionId;
	
	private List<String> effectIds = new ArrayList<>();
	
	// - - -
	public ACPolicyRequestDTO() {}
	
	public ACPolicyRequestDTO(String name, int priority, List<ACAccessType> accessTypes, String conditionId, List<String> effectIds, User owner) {
		this.name = name;
		this.priority = priority;
		this.accessTypes = accessTypes;
		this.conditionId = conditionId;
		this.effectIds = effectIds;
	}
	
	// - - -
	
	public String getName() {
		return name;
	}

	public ACPolicyRequestDTO setName(String name) {
		this.name = name;
		return this;
	}

	public int getPriority() {
		return priority;
	}

	public ACPolicyRequestDTO setPriority(int priority) {
		this.priority = priority;
		return this;
	}

	public List<ACAccessType> getAccessTypes() {
		return accessTypes;
	}

	public ACPolicyRequestDTO setAccessTypes(List<ACAccessType> accessTypes) {
		this.accessTypes = accessTypes;
		return this;
	}

	public String getConditionId() {
		return conditionId;
	}

	public ACPolicyRequestDTO setConditionId(String conditionId) {
		this.conditionId = conditionId;
		return this;
	}

	public List<String> getEffectIds() {
		return effectIds;
	}

	public ACPolicyRequestDTO setEffectIds(List<String> effectIds) {
		this.effectIds = effectIds;
		return this;
	}

}
