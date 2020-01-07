package org.citopt.connde.repository;

import org.citopt.connde.domain.device.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "devices", path = "devices")
public interface DeviceRepository extends UserEntityRepository<Device> {

    Device findByName(@Param("name") String name);

    @Override
    @PreAuthorize("@restSecurityGuard.checkPermission(#device, 'delete')")
    void delete(@Param("device") Device device);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@restSecurityGuard.retrieveUserEntities(returnObject, #pageable, @deviceRepository)")
    Page<Device> findAll(Pageable pageable);
}
