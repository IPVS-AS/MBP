package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Repository for environment models.
 */
@RepositoryRestResource(collectionResourceRel = "env-models", path = "env-models")
@Api(tags = {"Environment model entities"}, description = "CRUD for environment model entities")
public interface EnvironmentModelRepository extends UserEntityRepository<EnvironmentModel> {

    /**
     * Retrieves an environment model that is of a certain name from the repository.
     *
     * @param name The name of the environment model
     * @return The environment model
     */
    @RestResource(exported = false)
    EnvironmentModel findByName(@Param("name") String name);

    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#envModel, 'delete')")
    @ApiOperation(value = "Deletes an environment model entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the environment model entity"), @ApiResponse(code = 404, message = "Environment model entity not found")})
    void delete(@Param("envModel") @ApiParam(value = "The ID of the environment model entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) EnvironmentModel envModel);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @environmentModelRepository)")
    @ApiOperation(value = "Retrieves all environment model entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<EnvironmentModel> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}

