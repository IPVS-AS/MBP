package de.ipvs.as.mbp.domain.access_control.dto;

import de.ipvs.as.mbp.domain.access_control.ACAbstractCondition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Request DTO for {@link ACAbstractCondition}.
 * 
 * @author Jakob Benz
 */
public class ACConditionRequestDTO extends ACAbstractEntityRequestDTO {
	
	private String condition;
	
	// - - -
	
	public ACConditionRequestDTO() {}
	
	public ACConditionRequestDTO(String name, String description, String condition) {
		super(name, description);
		this.condition = condition;
	}
	
	// - - -
	
	public String getCondition() {
		return condition;
	}
	
	public ACConditionRequestDTO setCondition(String condition) {
		this.condition = condition;
		return this;
	}
}
