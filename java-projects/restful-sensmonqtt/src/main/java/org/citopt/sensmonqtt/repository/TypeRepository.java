package org.citopt.sensmonqtt.repository;

import org.citopt.sensmonqtt.domain.type.Type;
import org.citopt.sensmonqtt.repository.projection.TypeListProjection;
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

}
