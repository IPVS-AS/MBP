package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.repository.projection.ComponentTypeProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

/**
 * Spring Data MongoDB repository for the type entity.
 * @author Imeri Amil
 *
 */
@RepositoryRestResource(excerptProjection = ComponentTypeProjection.class)
public interface ComponentTypeRepository extends MongoRepository<ComponentType, String> {
	
    public ComponentType findByName(@Param("name") String name);
	
	public List<ComponentType> findAllByComponent(@Param("component") String component, Pageable pageable);

}
