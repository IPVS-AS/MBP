package org.citopt.connde.repository;

import java.util.List;
import java.util.Optional;

import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Abstract base interface for user entity repositories.
 *
 * @param <T> The type of the user entity
 */
@NoRepositoryBean
@ApiIgnore("Not an actual repository, only used as super interface")
public interface UserEntityRepository<T extends UserEntity> extends MongoRepository<T, String> {

    @RestResource(exported = false)
    @Query("{'_id': ?0}")
    Optional<T> get(String id);

    @Override
    @PostAuthorize("@repositorySecurityGuard.checkPermission(returnObject, 'read')")
    @ApiOperation(value = "Retrieves an entity by its ID", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the entity"), @ApiResponse(code = 404, message = "Entity not found")})
    Optional<T> findById(@ApiParam(value = "The ID of the entity", example = "5c97dc2583aeb6078c5ab672", required = true) String id);
    
    /**
	 * Retrieves all entities that are owned by the given user.
	 * 
	 * @param ownerId the id of the {@link User} that owns the entity.
	 * @param pageable the {@link Pageable} to configure the result set.
	 * @return a list holding all matching entities.
	 * @author Jakob Benz
	 */
	@Query("{ 'owner.id' : :#{#ownerId} }")
	List<T> findByOwner(@Param("ownerId") String ownerId, Pageable pageable);
	
//	/**
//	 * Retrieves all entities with at least one policy that matches all given access types.
//	 * 
//	 * @param accessTypes the list of access {@link ACAccessType types} as {@code String}.
//	 * @param pageable the {@link Pageable} to configure the result set.
//	 * @return a list holding all matching entities.
//	 * @author Jakob Benz
//	 */
//	@Query("{ 'accessControlPolicies' : { $elemMatch : { 'accessTypes' : { $all : :#{#accessTypes} } } } }")
//	List<T> findByPolicyAccessTypeMatchAll(@Param("accessTypes") List<String> accessTypes, Pageable pageable);
//	
//	
//	/**
//	 * Retrieves all entities with at least one policy that matches any of the given access types.
//	 * 
//	 * @param accessTypes the list of access {@link ACAccessType types} as {@code String}.
//	 * @param pageable the {@link Pageable} to configure the result set.
//	 * @return a list holding all matching entities.
//	 * @author Jakob Benz
//	 */
//	@Query("{ 'accessControlPolicies' : { $elemMatch : { 'accessTypes' : { $in : :#{#accessTypes} } } } }")
//	List<T> findByPolicyAccessTypeMatchAny(@Param("accessTypes") List<String> accessTypes, Pageable pageable);
//	
//	/**
//	 * Retrieves all entities that either are owned by the given user or
//	 * have at least one policy that matches all given access types.
//	 * 
//	 * @param ownerId the id of the {@link User} that owns the entity.
//	 * @param accessTypes the list of access {@link ACAccessType types} as {@code String}.
//	 * @param pageable the {@link Pageable} to configure the result set.
//	 * @return a list holding all matching entities.
//	 * @author Jakob Benz
//	 */
//	@Query("{ $or : [ { 'owner.id' : :#{#ownerId} }, { 'accessControlPolicies' : { $elemMatch : { 'accessTypes' : { $all : :#{#accessTypes} } } } } ] }")
//	List<T> findByOwnerOrPolicyAccessTypeMatchAll(@Param("ownerId") String ownerId, @Param("accessTypes") List<String> accessTypes, Pageable pageable);
//	
//	/**
//	 * Retrieves all entities that either are owned by the given user or
//	 * have at least one policy that matches any of the given access types.
//	 * 
//	 * @param ownerId the id of the {@link User} that owns the entity.
//	 * @param accessTypes the list of access {@link ACAccessType types} as {@code String}.
//	 * @param pageable the {@link Pageable} to configure the result set.
//	 * @return a list holding all matching entities.
//	 * @author Jakob Benz
//	 */
//	@Query("{ $or : [ { 'owner.id' : :#{#ownerId} }, { 'accessControlPolicies' : { $elemMatch : { 'accessTypes' : { $in : :#{#accessTypes} } } } } ] }")
//	List<T> findByOwnerOrPolicyAccessTypeMatchAny(@Param("ownerId") String ownerId, @Param("accessTypes") List<String> accessTypes, Pageable pageable);

    @Override
    @ApiOperation(value = "Saves a new or modified entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully saved"), @ApiResponse(code = 201, message = "Successfully created"), @ApiResponse(code = 403, message = "Not authorized to save the entity")})
    <S extends T> S save(@ApiParam(value = "Tne entity to save", required = true) S entity);

}
