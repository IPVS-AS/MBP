package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.componentType.ComponentType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data MongoDB repository for the type entity.
 * @author Imeri Amil
 *
 */
public interface ComponentTypeRepository extends MongoRepository<ComponentType, String> {
	
    public ComponentType findByName(@Param("name") String name);
	
	public List<ComponentType> findAllByComponent(@Param("component") String component, Pageable pageable);

}
