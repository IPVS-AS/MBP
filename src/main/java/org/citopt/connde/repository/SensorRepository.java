package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Sensor;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel = "sensors", path = "sensors")
public interface SensorRepository extends ComponentRepository<Sensor> {
}