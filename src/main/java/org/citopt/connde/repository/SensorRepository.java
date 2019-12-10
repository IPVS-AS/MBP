package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Sensor;
import org.springframework.data.rest.core.annotation.RestResource;

@RestResource(exported = false)
public interface SensorRepository extends ComponentRepository<Sensor> {

}