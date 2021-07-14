package de.ipvs.as.mbp.repository;

import java.util.List;

import de.ipvs.as.mbp.domain.access_control.ACAbstractCondition;
import de.ipvs.as.mbp.domain.access_control.ACPolicy;
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

	List<ACAbstractCondition> findByOwnerId(String ownerId);
	
	@Query(value = "{ name : :#{#name} }", exists = true)
	boolean existsByName(@Param("name") String name);
	
}