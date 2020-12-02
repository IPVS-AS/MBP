package de.ipvs.as.mbp.domain.access_control.dto;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.validation.constraints.NotEmpty;

import de.ipvs.as.mbp.domain.access_control.ACPolicy;
import de.ipvs.as.mbp.domain.user.User;

/**
 * Request DTO for {@link ACPolicy}. Used, e.g., for the request body
 * when creating new policies.
 * 
 * @author Jakob Benz
 */
public class ACPolicyRequestDTO extends ACAbstractEntityRequestDTO {
	
	@NotEmpty
	private List<String> accessTypes = new ArrayList<>();
	
	@Nonnull
	private String conditionId;
	
	private String effectId;
	
	// - - -
	public ACPolicyRequestDTO() {}
	
	public ACPolicyRequestDTO(String name, String description, List<String> accessTypes, String conditionId, String effectId, User owner) {
		super(name, description);
		this.accessTypes = accessTypes;
		this.conditionId = conditionId;
		this.effectId = effectId;
	}
	
	// - - -
	
	public List<String> getAccessTypes() {
		return accessTypes;
	}

	public ACPolicyRequestDTO setAccessTypes(List<String> accessTypes) {
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

	public String getEffectId() {
		return effectId;
	}

	public ACPolicyRequestDTO setEffectId(String effectId) {
		this.effectId = effectId;
		return this;
	}

}
