package org.citopt.connde.repository;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "actuators", path = "actuators")
public interface ActuatorRepository extends ComponentRepository<Actuator> {
    @Override
    @PreAuthorize("@restSecurityGuard.checkPermission(#actuator, 'delete')")
    void delete(@Param("actuator") Actuator actuator);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@restSecurityGuard.retrieveUserEntities(returnObject, #pageable, @actuatorRepository)")
    Page<Actuator> findAll(Pageable pageable);
}
