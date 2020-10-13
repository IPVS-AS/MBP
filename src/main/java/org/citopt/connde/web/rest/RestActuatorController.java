package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.service.UserEntityService;
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
 * REST Controller for managing {@link Actuator}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/actuators")
@Api(tags = { "Actuators" })
public class RestActuatorController {
	
    @Autowired
    private ActuatorRepository actuatorRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing actuator entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<Actuator>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding actuators (includes access-control)
    	List<Actuator> actuators = userEntityService.getPageWithPolicyCheck(actuatorRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(actuators, selfLink, pageable));
    }
    
    @GetMapping(path = "/{actuatorId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing actuator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the actuator!"),
    		@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
    public ResponseEntity<EntityModel<Actuator>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("actuatorId") String actuatorId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException {
    	// Retrieve the corresponding actuator (includes access-control)
    	Actuator actuator = userEntityService.getForIdWithPolicyCheck(actuatorRepository, actuatorId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(actuator));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing actuator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Actuator already exists!") })
    public ResponseEntity<EntityModel<Actuator>> create(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("actuatorId") String actuatorId, @ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody Actuator actuator) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save actuator in the database
    	Actuator createdActuator = userEntityService.create(actuatorRepository, actuator);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdActuator));
    }
    
    @DeleteMapping(path = "/{actuatorId}")
    @ApiOperation(value = "Deletes an existing actuator entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the actuator!"),
    		@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("actuatorId") String actuatorId) throws EntityNotFoundException {
    	// Delete the actuator (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(actuatorRepository, actuatorId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}
