package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.SensorTypeRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.entity_type.SensorType;
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
 * REST Controller for managing {@link SensorType}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(Constants.BASE_PATH + "/sensor-types")
@Api(tags = { "Sensor Types" })
public class RestSensorTypeController {
	
    @Autowired
    private SensorTypeRepository sensorTypeRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing sensor type entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Sensor type or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<SensorType>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding sensor types (includes access-control)
    	List<SensorType> sensorTypes = userEntityService.getPageWithAccessControlCheck(sensorTypeRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(sensorTypes, selfLink, pageable));
    }
    
    @GetMapping(path = "/{sensorTypeId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing sensor type entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the sensor type!"),
    		@ApiResponse(code = 404, message = "SensorType or requesting user not found!") })
    public ResponseEntity<EntityModel<SensorType>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("sensorTypeId") String sensorTypeId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
    	// Retrieve the corresponding sensor type (includes access-control)
    	SensorType sensorType = userEntityService.getForIdWithAccessControlCheck(sensorTypeRepository, sensorTypeId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(sensorType));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing sensor type entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Sensor type already exists!") })
    public ResponseEntity<EntityModel<SensorType>> create(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody SensorType sensorType) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save sensor type in the database
    	SensorType createdSensorType = userEntityService.create(sensorTypeRepository, sensorType);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdSensorType));
    }
    
    @DeleteMapping(path = "/{sensorTypeId}")
    @ApiOperation(value = "Deletes an existing sensor type entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the sensor type!"),
    		@ApiResponse(code = 404, message = "Sensor type or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("sensorTypeId") String sensorTypeId) throws EntityNotFoundException, MissingPermissionException {
    	// Delete the sensor type (includes access-control) 
    	userEntityService.deleteWithAccessControlCheck(sensorTypeRepository, sensorTypeId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}
