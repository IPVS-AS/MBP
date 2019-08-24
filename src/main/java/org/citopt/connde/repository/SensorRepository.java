package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Sensor;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface SensorRepository
        extends ComponentRepository<Sensor> {
}