package de.ipvs.as.mbp.service.validation;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;

/**
 * Interface for classes that validate whether a given {@link UserEntity} can be deleted.
 */
public interface IDeleteValidator<E extends UserEntity> {

    /**
     * Indicates whether an entity can be deleted, i.e., whether all
     * preconditions required for the delete operation are fulfilled.
     * If the entity cannot be deleted, an appropriate exception is thrown.
     *
     * @param entity the {@link UserEntity} to delete.
     */
    void validateDeletable(E entity);
}
