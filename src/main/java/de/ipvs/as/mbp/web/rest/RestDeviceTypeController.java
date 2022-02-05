package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceTypeRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.entity_type.DeviceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing {@link DeviceType}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(Constants.BASE_PATH + "/device-types")
@Api(tags = { "Devices Types" })
public class RestDeviceTypeController {
	
    @Autowired
    private DeviceTypeRepository deviceTypeRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing device type entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Device type or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<DeviceType>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding device types (includes access-control)
    	List<DeviceType> devices = userEntityService.getPageWithAccessControlCheck(deviceTypeRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(devices, selfLink, pageable));
    }
    
    @GetMapping(path = "/{deviceTypeId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device type entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the device type!"),
    		@ApiResponse(code = 404, message = "Device type or requesting user not found!") })
    public ResponseEntity<EntityModel<DeviceType>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("deviceTypeId") String deviceTypeId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding device type (includes access-control)
    	DeviceType deviceType = userEntityService.getForIdWithAccessControlCheck(deviceTypeRepository, deviceTypeId, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(deviceType));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device type entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Device type already exists!") })
    public ResponseEntity<EntityModel<DeviceType>> create(
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody DeviceType deviceType) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save device type in the database
    	DeviceType createdDeviceType = userEntityService.create(deviceTypeRepository, deviceType);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDeviceType));
    }
    
    @DeleteMapping(path = "/{deviceTypeId}")
    @ApiOperation(value = "Deletes an existing device type entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the device type!"),
    		@ApiResponse(code = 404, message = "Device type or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("deviceTypeId") String deviceTypeId) throws EntityNotFoundException, MissingPermissionException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Delete the device type (includes access-control) 
    	userEntityService.deleteWithAccessControlCheck(deviceTypeRepository, deviceTypeId, accessRequest);
    	return ResponseEntity.noContent().build();
    }
    
}
