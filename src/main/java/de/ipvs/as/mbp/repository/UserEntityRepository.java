package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import springfox.documentation.annotations.ApiIgnore;

import java.util.List;
import java.util.Optional;

/**
 * Abstract base interface for user entity repositories.
 *
 * @param <T> The type of the user entity
 */
@NoRepositoryBean
@ApiIgnore("Not an actual repository, only used as super interface")
public interface UserEntityRepository<T extends UserEntity> extends MongoRepository<T, String> {

    @Query("{ 'owner.id' : :#{#ownerId} }")
    List<T> findByOwner(@Param("ownerId") String ownerId, Pageable pageable);

    @Query("{ 'owner.id' : :#{#ownerId} }")
    List<T> findByOwner(@Param("ownerId") String ownerId);

    @Query(value = "{ 'name' : :#{#name} }", exists = true)
    boolean existsByName(@Param("name") String name);

    @Query("{ 'name' : :#{#name} }")
    Optional<T> findByName(@Param("name") String name);
}
