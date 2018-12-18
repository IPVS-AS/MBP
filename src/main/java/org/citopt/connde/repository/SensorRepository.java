package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SensorRepository
        extends ComponentRepository<Sensor> {
}