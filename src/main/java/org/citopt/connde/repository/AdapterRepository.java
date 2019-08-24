package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.repository.projection.AdapterExcerpt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 *
 * @author rafaelkperes
 */
@RepositoryRestResource(excerptProjection = AdapterExcerpt.class)
public interface AdapterRepository
        extends MongoRepository<Adapter, String> {

    Adapter findByName(@Param("name") String name);

    List<Adapter> findAllByNameContainingIgnoreCase(@Param("name") String name);

}
