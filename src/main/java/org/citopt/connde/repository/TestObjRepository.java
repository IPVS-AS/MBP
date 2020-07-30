package org.citopt.connde.repository;

import org.citopt.connde.domain.access_control.TestObj;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestObjRepository extends MongoRepository<TestObj, String>
//extends UserEntityRepository<?> 
{
	
//	@Query("{ 'owner.id' : :#{#ownerId} }")
//	List<TestObj> findByOwner(@Param("ownerId") String ownerId, Pageable pageable);
//	
//	@Query("{ 'policies' : { $elemMatch: { accessTypes: { $all: ?0 } } } }")
//	List<TestObj> findByPolicyAccessTypeMatchAll(@Param("accessTypes") List<String> accessTypes, Pageable pageable);
//	
//	@Query("{ 'policies' : { $elemMatch: { accessTypes: { $in: ?0 } } } }")
//	List<TestObj> findByPolicyAccessTypeMatchAny(@Param("accessTypes") List<String> accessTypes, Pageable pageable);
//	
//	@Query("{ $and: [ { 'owner.id' : :#{#ownerId} }, { 'policies' : { $elemMatch: { accessTypes: { $all: ?1 } } } } ] }")
//	List<TestObj> findByOwnerOrPolicyAccessTypeMatchAll(@Param("ownerId") String ownerId, @Param("accessTypes") List<String> accessTypes, Pageable pageable);

}
