package org.citopt.connde.repository;

import org.citopt.connde.domain.adapter.Adapter;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

@RestResource(exported = false)
public interface AdapterRepository extends UserEntityRepository<Adapter> {
    Adapter findByName(@Param("name") String name);
}
