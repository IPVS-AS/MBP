package org.citopt.sensmonqtt.repository;

import org.citopt.sensmonqtt.domain.component.Component;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource
public interface ComponentRepository
        extends MongoRepository<Component, String> {
    
    public Component findByName(@Param("name") String name);

}
