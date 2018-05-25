package org.citopt.connde.repository;

import java.util.List;
import org.citopt.connde.domain.type.Type;
import org.citopt.connde.repository.projection.TypeListProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource(excerptProjection = TypeListProjection.class)
public interface TypeRepository
        extends MongoRepository<Type, String> {

    public Type findByName(@Param("name") String name);

    public List<Type> findAllByNameContainingIgnoreCase(@Param("name") String name);

}
