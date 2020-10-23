package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.entity_type.SensorType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "sensor-types", path = "sensor-types")
@Api(tags = {"Sensor types"}, description = "CRUD for sensor types")
public interface SensorTypeRepository extends EntityTypeRepository<SensorType> {
    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#sensorType, 'delete')")
    @ApiOperation(value = "Deletes a sensor type", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the sensor type"), @ApiResponse(code = 404, message = "Sensor type not found")})
    void delete(@Param("sensorType") @ApiParam(value = "The ID of the sensor type to delete", example = "5c97dc2583aeb6078c5ab672", required = true) SensorType sensorType);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @sensorTypeRepository)")
    @ApiOperation(value = "Retrieves all sensor types for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<SensorType> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}