package org.citopt.connde.domain.access_control.dto;

import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Abstract base class access-control related request DTOs.
 * 
 * @author Jakob Benz
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class ACAbstractEntityRequestDTO {
	
	@NotEmpty
	private String name;
	
	private String description;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAbstractEntityRequestDTO() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this entity.
	 * @param description the description of this entity.
	 */
	public ACAbstractEntityRequestDTO(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	// - - -
	
	public String getName() {
		return name;
	}
	
	public ACAbstractEntityRequestDTO setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ACAbstractEntityRequestDTO setDescription(String description) {
		this.description = description;
		return this;
	}
	
}
