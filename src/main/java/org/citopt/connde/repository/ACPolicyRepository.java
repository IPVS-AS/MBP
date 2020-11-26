package org.citopt.connde.repository;

import java.util.List;
import java.util.Optional;

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
public interface ACPolicyRepository extends MongoRepository<ACPolicy, String> {
	
	@Query("{ 'ownerId' : :#{#ownerId} }")
	List<ACPolicy> findAllByOwner(@Param("ownerId") String ownerId, Pageable pageable);
	
	@Query(value = "{ name : :#{#name} }", exists = true)
	boolean existsByName(@Param("name") String name);
	
	@Query(value = "{ 'conditionId' : :#{#conditionId} }", count = true)
	public Long countUsingCondition(@Param("conditionId") String conditionId);
	
	@Query(value = "{ 'conditionId' : :#{#effectId} }", count = true)
	public Long countUsingEffect(@Param("effectId") String effectId);
	
	@Query("{ $and : [ { 'ownerId' : :#{#ownerId} }, { 'conditionId' : :#{#conditionId} } ] }")
	List<ACPolicy> findByOwnerAndCondition(@Param("ownerId") String ownerId, @Param("conditionId") String conditionId, Pageable pageable);
	
	@Query("{ $and : [ { 'ownerId' : :#{#ownerId} }, { 'effectId' : :#{#effectId} } ] }")
	List<ACPolicy> findByOwnerAndEffect(@Param("ownerId") String ownerId, @Param("effectId") String effectId, Pageable pageable);
	
//	@Query("{ $and : [ { 'owner.id' : :#{#ownerId} }, { 'effects' : { $elemMatch : { 'id' : :#{#effectId} } } } ] }")
//	List<ACPolicy> findByOwnerAndEffectAny(@Param("ownerId") String ownerId, @Param("effectId") String effectId, Pageable pageable);
	
	@Query("{ 'id' : { $in : :#{#ids} } }")
	List<ACPolicy> findByIdAny(@Param("ids") List<String> ids);
	
	@Query("{ 'accessTypes' : { $all : :#{#accessTypes} } }")
	List<ACPolicy> findByAccessTypeAll(@Param("accessTypes") List<String> accessTypes);
	
	@Query("{ $and : [ { 'id' : :#{#id} }, { 'accessTypes' : { $all : :#{#accessTypes} } } ] }")
	Optional<ACPolicy> findByIdAndAccessTypeAll(@Param("id") String id, @Param("accessTypes") List<String> accessTypes);
	
	@Query(value = "{ $and : [ { 'id' : :#{#id} }, { 'accessTypes' : { $all : :#{#accessTypes} } } ] }", exists = true)
	boolean existsByIdAndAccessTypeAll(@Param("id") String id, @Param("accessTypes") List<String> accessTypes);
	
	@Query("{ $and : [ { 'id' : { $in : :#{#ids} } }, { 'accessTypes' : { $all : :#{#accessTypes} } } ] }")
	List<ACPolicy> findByIdAnyAndAccessTypeAll(@Param("ids") List<String> ids, @Param("accessTypes") List<String> accessTypes);
	
	@Query(value = "{ $and : [ { 'id' : { $in : :#{#ids} } }, { 'accessTypes' : { $all : :#{#accessTypes} } } ] }", exists = true)
	boolean existsByIdAnyAndAccessTypeAll(@Param("ids") List<String> ids, @Param("accessTypes") List<String> accessTypes);
	
}