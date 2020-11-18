package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.ActuatorTypeRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.entity_type.ActuatorType;
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
 * REST Controller for managing {@link ActuatorType}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/actuator-types")
@Api(tags = { "Actuator Types" })
public class RestActuatorTypeController {
	
    @Autowired
    private ActuatorTypeRepository actuatorTypeRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing actuator type entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Actuator type or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ActuatorType>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding actuator types (includes access-control)
    	List<ActuatorType> actuatorTypes = userEntityService.getPageWithAccessControlCheck(actuatorTypeRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(actuatorTypes, selfLink, pageable));
    }
    
    @GetMapping(path = "/{actuatorTypeId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing actuator type entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the actuator type!"),
    		@ApiResponse(code = 404, message = "ActuatorType or requesting user not found!") })
    public ResponseEntity<EntityModel<ActuatorType>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("actuatorTypeId") String actuatorTypeId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
    	// Retrieve the corresponding actuator type (includes access-control)
    	ActuatorType actuatorType = userEntityService.getForIdWithAccessControlCheck(actuatorTypeRepository, actuatorTypeId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(actuatorType));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing actuator type entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Actuator type already exists!") })
    public ResponseEntity<EntityModel<ActuatorType>> create(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody ActuatorType actuatorType) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save actuator type in the database
    	ActuatorType createdActuatorType = userEntityService.create(actuatorTypeRepository, actuatorType);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdActuatorType));
    }
    
    @DeleteMapping(path = "/{actuatorTypeId}")
    @ApiOperation(value = "Deletes an existing actuator type entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the actuator type!"),
    		@ApiResponse(code = 404, message = "Actuator type or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("actuatorTypeId") String actuatorTypeId) throws EntityNotFoundException, MissingPermissionException {
    	// Delete the actuator type (includes access-control) 
    	userEntityService.deleteWithAccessControlCheck(actuatorTypeRepository, actuatorTypeId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}
