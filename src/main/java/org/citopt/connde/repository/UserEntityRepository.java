package org.citopt.connde.repository;

import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

/**
 * Base interface for user entity repositories.
 *
 * @param <T> The type of the user entity
 */
public interface UserEntityRepository<T extends UserEntity> extends MongoRepository<T, String> {
}
