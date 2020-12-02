package de.ipvs.as.mbp.domain.access_control.dto;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotEmpty;

/**
 * Request DTO for {@link ACAbstractEffect}.
 * 
 * @author Jakob Benz
 */
public class ACEffectRequestDTO extends ACAbstractEntityRequestDTO {
	
	@NotEmpty
	private Map<String, String> parameters = new HashMap<>();

	private String type;
	
	// - - -
	
	public ACEffectRequestDTO() {}
	
	public ACEffectRequestDTO(String name, String description, Map<String, String> parameters, String type) {
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
	
	public String getType() {
		return type;
	}
	
	public ACEffectRequestDTO setType(String type) {
		this.type = type;
		return this;
	}

}
