package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource
public interface ActuatorRepository
        extends MongoRepository<Actuator, String> {
    
    public Actuator findByName(@Param("name") String name);

}
