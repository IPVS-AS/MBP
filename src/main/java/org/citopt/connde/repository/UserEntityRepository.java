package org.citopt.connde.repository;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;

import java.util.List;

/**
 * Base interface for user entity repositories.
 *
 * @param <T> The type of the user entity
 */
public interface UserEntityRepository<T extends UserEntity> extends MongoRepository<T, String> {

    @Override
    @PostAuthorize("@restSecurityGuard.checkPermission(returnObject, 'read')")
    T findOne(String id);

    @RestResource(exported = false)
    @Query("{'_id': ?0}")
    T get(String id);

    @Override
    @RestResource(exported = false)
    List<T> findAll();

    @Override
    @RestResource(exported = false)
    List<T> findAll(Sort var1);

    @Override
    @RestResource(exported = false)
    <S extends T> List<S> findAll(Example<S> var1);

    @Override
    @RestResource(exported = false)
    <S extends T> List<S> findAll(Example<S> var1, Sort var2);

    @Override
    @RestResource(exported = false)
    Iterable<T> findAll(Iterable<String> ids);

    @Override
    @RestResource(exported = false)
    <S extends T> List<S> save(Iterable<S> ids);

    @Override
    @RestResource(exported = false)
    void delete(Iterable<? extends T> ids);

    @Override
    @RestResource(exported = false)
    void deleteAll();

    @Override
    @RestResource(exported = false)
    boolean exists(String id);

    @Override
    @RestResource(exported = false)
    long count();
}
