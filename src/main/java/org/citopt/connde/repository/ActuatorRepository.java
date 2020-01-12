package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.component.Actuator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "actuators", path = "actuators")
@Api(tags = {"Actuator entities"}, description = "CRUD for actuator entities")
public interface ActuatorRepository extends ComponentRepository<Actuator> {
    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#actuator, 'delete')")
    @ApiOperation(value = "Deletes an actuator entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the actuator entity"), @ApiResponse(code = 404, message = "Actuator entity not found")})
    void delete(@Param("actuator") @ApiParam(value = "The ID of the actuator entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Actuator actuator);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @actuatorRepository)")
    @ApiOperation(value = "Retrieves all actuator entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<Actuator> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
