package de.ipvs.as.mbp.service.validation;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;

/**
 * @author Jakob Benz
 */
public interface IDeleteValidator<E extends UserEntity> {
	
//	/**
//	 * Indicates whether an entity is being used by other entities.
//	 * 
//	 * @return {@code true} if and only if an entity is being used
//	 * 		   by at least one other entity; {@code false} otherwise.
//	 */
//	public boolean isStillInUse();
	
	/**
	 * Indicates whether an entity can be deleted, i.e., whether all
	 * preconditions required for the delete operation are fulfilled.
	 * If the entity cannot be deleted, an appropriate exception is thrown.
	 * 
	 * @param entity the {@link UserEntity} to delete.
	 */
	void validateDeletable(E entity);

}
