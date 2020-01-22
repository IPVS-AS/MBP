package org.citopt.connde.repository;

import io.swagger.annotations.*;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.projection.DeviceExcerpt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;

@RepositoryRestResource(collectionResourceRel = "devices", path = "devices", excerptProjection = DeviceExcerpt.class)
@Api(tags = {"Device entities"}, description = "CRUD for device entities")
public interface DeviceRepository extends UserEntityRepository<Device> {
    @RestResource(exported = false)
    Device findByName(@Param("name") String name);

    @Override
    @PreAuthorize("@repositorySecurityGuard.checkPermission(#device, 'delete')")
    @ApiOperation(value = "Deletes a device entity", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 204, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to delete the device entity"), @ApiResponse(code = 404, message = "Device entity not found")})
    void delete(@Param("device") @ApiParam(value = "The ID of the device entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Device device);

    @Override
    @Query("{_id: null}") //Fail fast
    @PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @deviceRepository)")
    @ApiOperation(value = "Retrieves all device entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    Page<Device> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);
}
