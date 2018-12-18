package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Actuator;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ActuatorRepository
        extends ComponentRepository<Actuator> {
}
