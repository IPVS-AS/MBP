package org.citopt.connde.repository;

import org.citopt.connde.domain.device.Device;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RepositoryRestResource(collectionResourceRel = "devices", path = "devices")
public interface DeviceRepository extends UserEntityRepository<Device> {

    Device findByName(@Param("name") String name);

    @Override
    @RestResource(exported = false)
    List<Device> findAll();

    @Override
    @RestResource(exported = false)
    List<Device> findAll(Sort var1);

    @Override
    @RestResource(exported = false)
    <S extends Device> List<S> findAll(Example<S> var1);

    @Override
    @RestResource(exported = false)
    <S extends Device> List<S> findAll(Example<S> var1, Sort var2);

    @Override
    @RestResource(exported = false)
    Iterable<Device> findAll(Iterable<String> var1);

    @Override
    @PreAuthorize("@userSecurityCheck.check(#device, 'delete')")
    void delete(@Param("device") Device device);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@userSecurityCheck.checkPage(returnObject, #pageable, @deviceRepository)")
    Page<Device> findAll(Pageable pageable);
}
