package org.citopt.connde.repository;

import org.citopt.connde.domain.adapter.Adapter;
import org.springframework.data.repository.query.Param;

public interface AdapterRepository extends UserEntityRepository<Adapter> {

    Adapter findByName(@Param("name") String name);
}
