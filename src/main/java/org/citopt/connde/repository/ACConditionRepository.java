package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.user.User;
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
	
	/**
	 * Retrieves all conditions that are owned by the given user.
	 * 
	 * @param ownerId the id of the {@link User} that owns the entity.
	 * @param pageable the {@link Pageable} to configure the result set.
	 * @return a list holding all matching {@link ACAbstractCondition} entities.
	 * @author Jakob Benz
	 */
	@Query("{ 'ownerId' : :#{#ownerId} }")
	List<ACAbstractCondition> findByOwner(@Param("ownerId") String ownerId, Pageable pageable);
	
	@Query(value = "{ name : :#{#name} }", exists = true)
	boolean existsByName(@Param("name") String name); 
	
}