package org.citopt.connde.repository;

import java.util.List;

import org.citopt.connde.domain.device.Device;
import org.springframework.data.repository.query.Param;

//@RepositoryRestResource(collectionResourceRel = "devices", path = "devices", excerptProjection = DeviceExcerpt.class)
//@Api(tags = { "Device entities" }, description = "CRUD for device entities")
public interface DeviceRepository extends UserEntityRepository<Device> {
	
//	@RestResource(exported = false)
	// TODO: Return type should be changed to Optional<Device> (current version is heavily used in testing tool ...)
//	Device findByName(@Param("name") String name);

//	@Override
//	@PreAuthorize("@repositorySecurityGuard.checkPermission(#device, 'delete')")
//	@ApiOperation(value = "Deletes a device entity", produces = "application/hal+json")
//	@ApiResponses({ @ApiResponse(code = 204, message = "Success"),
//			@ApiResponse(code = 403, message = "Not authorized to delete the device entity"),
//			@ApiResponse(code = 404, message = "Device entity not found") })
//	void delete(@Param("device") @ApiParam(value = "The ID of the device entity to delete", example = "5c97dc2583aeb6078c5ab672", required = true) Device device);

//	@Override
//	@Query("{_id: null}") // Fail fast
//	@PostAuthorize("@repositorySecurityGuard.retrieveUserEntities(returnObject, #pageable, @deviceRepository)")
//	@ApiOperation(value = "Retrieves all device entities for which the user is authorized and which fit onto a given page", produces = "application/hal+json")
//	@ApiResponses({ @ApiResponse(code = 200, message = "Success") })
//	Page<Device> findAll(@ApiParam(value = "The page configuration", required = true) Pageable pageable);

//	@RestResource(exported = false)
	List<Device> findAllByKeyPairId(@Param("keyPair.id") String keyPairId);

}
