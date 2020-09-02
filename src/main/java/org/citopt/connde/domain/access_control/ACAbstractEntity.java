package org.citopt.connde.domain.access_control;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.domain.user.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract base class access-control related domain objects, e.g., policies or conditions.
 * 
 * @author Jakob Benz
 */
public abstract class ACAbstractEntity {
	
	/**
	 * The id of this entity.
	 */
	@Id
    @GeneratedValue
	private String id;
	
	/**
	 * The name of this entity.
	 */
	@NotEmpty
	private String name;
	
	/**
	 * The description of this entity.
	 */
	@NotEmpty
	private String description;
	
	/**
	 * The {@link User} that owns this entity.
	 */
	@JsonIgnore
	@DBRef
	private User owner;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAbstractEntity() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param name the name of this entity.
	 * @param description the description of this entity.
	 * @param owner the {@link User} that owns this entity.
	 */
	public ACAbstractEntity(String name, String description, User owner) {
		this.name = name;
		this.description = description;
		this.owner = owner;
	}
	
	// - - -
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public ACAbstractEntity setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ACAbstractEntity setDescription(String description) {
		this.description = description;
		return this;
	}
	
	public User getOwner() {
		return owner;
	}
	
	public ACAbstractEntity setOwner(User owner) {
		this.owner = owner;
		return this;
	}
	
}
