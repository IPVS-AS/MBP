package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository for access-control {@link ACPolicy policies}.
 * 
 * @author Jakob Benz
 */
@Repository
public interface ACConditionRepository extends MongoRepository<ACAbstractCondition, String> {
	
	@Query("{ 'ownerId' : :#{#ownerId} }")
	List<ACAbstractCondition> findAllByOwner(@Param("ownerId") String ownerId, Pageable pageable);
	
	@Query(value = "{ name : :#{#name} }", exists = true)
	boolean existsByName(@Param("name") String name); 
	
}