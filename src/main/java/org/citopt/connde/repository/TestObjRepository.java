package org.citopt.connde.repository;

import org.citopt.connde.domain.access_control.TestObj;
import org.springframework.stereotype.Repository;

@Repository
public interface TestObjRepository extends UserEntityRepository<TestObj> {
	
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
