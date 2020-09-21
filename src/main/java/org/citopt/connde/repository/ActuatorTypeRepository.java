package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.entity_type.ActuatorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "actuator-types", path = "actuator-types")
@Api(tags = {"actuator types"}, description = "CRUD for actuator types")
public interface ActuatorTypeRepository extends EntityTypeRepository<ActuatorType> {
    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#actuatorType, 'delete')")
    @ApiOperation(value = "Deletes an actuator type", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the actuator type"), @ApiResponse(code = 404, message = "actuator type not found")})
    void delete(@Param("actuatorType") @ApiParam(value = "The ID of the actuator type to delete", example = "5c97dc2583aeb6078c5ab672", required = true) ActuatorType actuatorType);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @actuatorTypeRepository)")
    @ApiOperation(value = "Retrieves all actuator types for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<ActuatorType> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}