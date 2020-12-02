package de.ipvs.as.mbp.repository;

import java.util.List;

import de.ipvs.as.mbp.domain.access_control.ACAbstractEffect;
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
public interface ACEffectRepository extends MongoRepository<ACAbstractEffect, String> {
	
	@Query("{ 'ownerId' : :#{#ownerId} }")
	List<ACAbstractEffect> findAllByOwner(@Param("ownerId") String ownerId, Pageable pageable);
	
	@Query(value = "{ name : :#{#name} }", exists = true)
	boolean existsByName(@Param("name") String name); 
	
}