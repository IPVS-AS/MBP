package org.citopt.connde.domain.access_control;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotEmpty;

import org.citopt.connde.domain.user.User;
import org.springframework.data.annotation.Id;

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
	 * The id of the {@link User} that owns this entity.
	 */
	@NotEmpty
	private String ownerId;
	
	// NOTE: DBRefs are not used here, since Spring Data MongoDB lacks the support for DBRefs in retrieving queries.
	//       Furthermore, even the MongoDB guys themselves do not recommend using DBRefs (e.g., see https://jira.spring.io/browse/DATAMONGO-1584)
	//       since DBRefs basically are joins and apparently MongoDB isn't very good at that. Instead we simply store the id of the embedded
	//       documents and do the lookup on the application level. From a performance / overhead point of view it's basically the same,
	//       since we now have to do the lookup when retrieving entities but not when persisting (creating) them.
	
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
	 * @param ownerId the id of the {@link User} that owns this entity.
	 */
	public ACAbstractEntity(String name, String description, String ownerId) {
		this.name = name;
		this.description = description;
		this.ownerId = ownerId;
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

	public String getOwnerId() {
		return ownerId;
	}
	
	public ACAbstractEntity setOwnerId(String ownerId) {
		this.ownerId = ownerId;
		return this;
	}
	
}
