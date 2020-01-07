package org.citopt.connde.repository;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "adapters", path = "adapters")
public interface AdapterRepository extends UserEntityRepository<Adapter> {
    Adapter findByName(@Param("name") String name);

    @Override
    @PreAuthorize("@restSecurityGuard.checkPermission(#adapter, 'delete')")
    void delete(@Param("adapter") Adapter adapter);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@restSecurityGuard.retrieveUserEntities(returnObject, #pageable, @adapterRepository)")
    Page<Adapter> findAll(Pageable pageable);
}
