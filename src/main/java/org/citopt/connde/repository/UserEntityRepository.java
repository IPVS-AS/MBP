package org.citopt.connde.repository;

import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Base interface for user entity repositories.
 *
 * @param <T> The type of the user entity
 */
public interface UserEntityRepository<T extends UserEntity> extends MongoRepository<T, String> {

}
