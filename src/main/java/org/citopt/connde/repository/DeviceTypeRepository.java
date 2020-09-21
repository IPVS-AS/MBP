package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.entity_type.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "device-types", path = "device-types")
@Api(tags = {"Device types"}, description = "CRUD for device types")
public interface DeviceTypeRepository extends EntityTypeRepository<DeviceType> {
    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#deviceType, 'delete')")
    @ApiOperation(value = "Deletes a device type", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the device type"), @ApiResponse(code = 404, message = "Device type not found")})
    void delete(@Param("deviceType") @ApiParam(value = "The ID of the device type to delete", example = "5c97dc2583aeb6078c5ab672", required = true) DeviceType deviceType);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @deviceTypeRepository)")
    @ApiOperation(value = "Retrieves all device types for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<DeviceType> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}