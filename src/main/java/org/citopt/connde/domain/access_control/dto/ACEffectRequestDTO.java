package org.citopt.connde.domain.access_control.dto;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

import org.citopt.connde.domain.access_control.ACAbstractEffect;
import org.citopt.connde.domain.access_control.ACEffectType;

/**
 * Request DTO for {@link ACAbstractEffect}.
 * 
 * @author Jakob Benz
 */
public class ACEffectRequestDTO extends ACAbstractEntityRequestDTO {
	
	@NotEmpty
	private Map<String, String> parameters = new HashMap<>();

	private ACEffectType type;
	
	// - - -
	
	public ACEffectRequestDTO() {}
	
	public ACEffectRequestDTO(String name, String description, Map<String, String> parameters, ACEffectType type) {
		super(name, description);
		this.parameters = parameters;
		this.type = type;
	}
	
	// - - -
	
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	public ACEffectRequestDTO setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return this;
	}
	
	public ACEffectType getType() {
		return type;
	}
	
	public ACEffectRequestDTO setType(ACEffectType type) {
		this.type = type;
		return this;
	}

}
