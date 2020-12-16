package de.ipvs.as.mbp.service.validation;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;

/**
 * Interface for create validators that validate user entities that are supposed to be created.
 *
 * @param <E> The type of user entities the validator is responsible for
 */
public interface ICreateValidator<E extends UserEntity> {
    /**
     * Validates a given entity that is supposed to be created and throws an exception with explanations
     * in case fields are invalid.
     *
     * @param entity The entity to validate on creation
     */
    void validateCreatable(E entity);
}
