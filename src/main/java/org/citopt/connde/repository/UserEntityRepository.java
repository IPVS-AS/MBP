package org.citopt.connde.repository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
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
    T get(String id);

    @Override
    @PostAuthorize("@restSecurityGuard.checkPermission(returnObject, 'read')")
    @ApiOperation(value = "Retrieves an entity by its ID", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the entity"), @ApiResponse(code = 404, message = "Entity not found")})
    T findOne(@ApiParam(value = "The ID of the entity", example = "5c97dc2583aeb6078c5ab672", required = true) String id);

    @Override
    @ApiOperation(value = "Saves a new or modified entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Successfully saved"), @ApiResponse(code = 201, message = "Successfully created"), @ApiResponse(code = 403, message = "Not authorized to save the entity")})
    <S extends T> S save(@ApiParam(value = "Tne entity to save", required = true) S entity);

}
