package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.repository.projection.ComponentTypeExcerpt;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Spring Data MongoDB repository for the component type entity.
 */
@RepositoryRestResource(excerptProjection = ComponentTypeExcerpt.class)
public interface ComponentTypeRepository extends MongoRepository<ComponentType, String> {

    ComponentType findByName(@Param("name") String name);

    List<ComponentType> findAllByComponent(@Param("component") String component, Pageable pageable);

}
