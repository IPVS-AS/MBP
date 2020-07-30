package org.citopt.connde.domain.access_control;

import javax.annotation.Nonnull;

/**
 * Abstraction for certain access requests. Wraps an {@link ACAccessType}
 * and the {@link IACRequestedEntity entity} the access is requested for.
 * 
 * @author Jakob Benz
 */
public class ACAccess {
	
	/**
	 *  The {@link ACAccessType type} of this access.
	 */
	@Nonnull
	private ACAccessType type;
	
	/**
	 * The {@link IACRequestingEntity entity} that requested the access.
	 */
	@Nonnull
	private IACRequestingEntity requestingEntity;
	
	/**
	 * The {@link IACRequestedEntity entity} this access is requested for.
	 */
	@Nonnull
	private IACRequestedEntity requestedEntity;
	
	// - - -
	
	/**
	 * No-args constructor.
	 */
	public ACAccess() {}
	
	/**
	 * All-args constructor.
	 * 
	 * @param type the {@link ACAccessType type} of this access.
	 * @param requestingEntity the {@link IACRequestingEntity entity} that requested the access.
	 * @param requestedEntity the {@link IACRequestedEntity entity} this access is requested for.
	 */
	public ACAccess(ACAccessType type, IACRequestingEntity requestingEntity, IACRequestedEntity requestedEntity) {
		this.type = type;
		this.requestingEntity = requestingEntity;
		this.requestedEntity = requestedEntity;
	}
	
	// - - -

	public ACAccessType getType() {
		return type;
	}

	public ACAccess setType(ACAccessType type) {
		this.type = type;
		return this;
	}

	public IACRequestingEntity getRequestingEntity() {
		return requestingEntity;
	}

	public ACAccess setRequestingEntity(IACRequestingEntity requestingEntity) {
		this.requestingEntity = requestingEntity;
		return this;
	}

	public IACRequestedEntity getRequestedEntity() {
		return requestedEntity;
	}

	public ACAccess setRequestedEntity(IACRequestedEntity requestedEntity) {
		this.requestedEntity = requestedEntity;
		return this;
	}
	
	// - - -
	
	/**
	 * Returns the entity of this access associated with the given entity type.
	 * 
	 * @param entityType the {@link ACEntityType}.
	 * @return the {@link #requestingEntity} in case the entity type is {@link ACEntityType#REQUESTING_ENTITY};
	 * 		   the {@link #requestedEntity} otherwise.
	 */
	public IACEntity getEntityForType(ACEntityType entityType) {
		return entityType == ACEntityType.REQUESTING_ENTITY ? requestingEntity : requestedEntity;
	}

}
