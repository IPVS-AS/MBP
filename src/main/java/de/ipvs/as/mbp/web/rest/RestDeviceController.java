package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.KeyPairRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceDTO;
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
 * REST Controller for managing {@link Device}s.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/devices")
@Api(tags = { "Devices" })
public class RestDeviceController {
	
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private KeyPairRepository keyPairRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing device entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({
			@ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Device or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<Device>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding devices (includes access-control)
    	List<Device> devices = userEntityService.getPageWithAccessControlCheck(deviceRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(devices, selfLink, pageable));
    }
    
    @GetMapping(path = "/{deviceId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the device!"),
    		@ApiResponse(code = 404, message = "Device or requesting user not found!") })
    public ResponseEntity<EntityModel<Device>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("deviceId") String deviceId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding device (includes access-control)
    	Device device = userEntityService.getForIdWithAccessControlCheck(deviceRepository, deviceId, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(device));
    }
    
    @GetMapping("/by-key/{id}")
    @ApiOperation(value = "Retrieves the devices which use a certain key-pair and for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Key-pair or requesting user not found!")})
    public ResponseEntity<List<Device>> byKeyPair(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the key-pair", example = "5c97dc2583aeb6078c5ab672", required = true) String keyPairId) throws EntityNotFoundException, MissingPermissionException {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Check permission for key-pair
        userEntityService.requirePermission(keyPairRepository, keyPairId, ACAccessType.READ, accessRequest);

        // Retrieve all devices from the database (includes access-control)
        List<Device> devices = userEntityService.getAllWithAccessControlCheck(deviceRepository, ACAccessType.READ, accessRequest)
                .stream()
                // Filter devices that do not use the key-pair
                .filter(d -> d.hasRSAKey() && d.getKeyPair().getId().equals(keyPairId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(devices);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing device entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Device already exists!") })
    public ResponseEntity<EntityModel<Device>> create(
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody DeviceDTO requestDto) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Create device from request DTO
    	Device device = (Device) new Device()
    			.setName(requestDto.getName())
    			.setComponentType(requestDto.getComponentType())
    			.setIpAddress(requestDto.getIpAddress())
    			.setDate(LocalDateTime.now().toString())
    			.setUsername(requestDto.getUsername())
    			.setPassword(requestDto.getPassword() == null ? null : requestDto.getPassword())
    			.setKeyPair(requestDto.getKeyPairId() == null ? null : userEntityService.getForId(keyPairRepository, requestDto.getKeyPairId()))
    			.setAccessControlPolicyIds(requestDto.getAccessControlPolicyIds());
    	
    	// Save device in the database
    	Device createdDevice = userEntityService.create(deviceRepository, device);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdDevice));
    }
    
    @DeleteMapping(path = "/{deviceId}")
    @ApiOperation(value = "Deletes an existing device entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the device!"),
    		@ApiResponse(code = 404, message = "Device or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("deviceId") String deviceId) throws EntityNotFoundException, MissingPermissionException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Delete the device (includes access-control) 
    	userEntityService.deleteWithAccessControlCheck(deviceRepository, deviceId, accessRequest);
    	return ResponseEntity.noContent().build();
    }
}
