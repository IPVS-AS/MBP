package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "sensors", path = "sensors")
public interface SensorRepository extends ComponentRepository<Sensor> {
    @Override
    @PreAuthorize("@restSecurityGuard.checkPermission(#sensor, 'delete')")
    void delete(@Param("sensor") Sensor sensor);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@restSecurityGuard.retrieveUserEntities(returnObject, #pageable, @sensorRepository)")
    Page<Sensor> findAll(Pageable pageable);
}