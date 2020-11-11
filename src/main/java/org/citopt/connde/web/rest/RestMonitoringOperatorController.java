package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.monitoring.MonitoringOperator;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.MonitoringOperatorRepository;
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
 * REST Controller for managing {@link MonitoringOperator}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/monitoring-operators")
@Api(tags = { "Monitoring Operators" })
public class RestMonitoringOperatorController {
	
    @Autowired
    private MonitoringOperatorRepository monitoringOperatorRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing monitoring operator entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Monitoring monitoring operator or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<MonitoringOperator>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding monitoring operators (includes access-control)
    	List<MonitoringOperator> monitoringOperators = userEntityService.getPageWithAccessControlCheck(monitoringOperatorRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(monitoringOperators, selfLink, pageable));
    }
    
    @GetMapping(path = "/{monitoringOperatorId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing monitoring operator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the monitoring operator!"),
    		@ApiResponse(code = 404, message = "Monitoring operator or requesting user not found!") })
    public ResponseEntity<EntityModel<MonitoringOperator>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("monitoringOperatorId") String monitoringOperatorId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
    	// Retrieve the corresponding monitoring operator (includes access-control)
    	MonitoringOperator monitoringOperator = userEntityService.getForIdWithAccessControlCheck(monitoringOperatorRepository, monitoringOperatorId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(monitoringOperator));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing monitoring operator entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Monitoring operator already exists!") })
    public ResponseEntity<EntityModel<MonitoringOperator>> create(@PathVariable("monitoringOperatorId") String monitoringOperatorId, @ApiParam(value = "Page parameters", required = true) Pageable pageable, @RequestBody MonitoringOperator monitoringOperator) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save monitoring operator in the database
    	MonitoringOperator createdMonitoringOperator = userEntityService.create(monitoringOperatorRepository, monitoringOperator);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdMonitoringOperator));
    }
    
    @DeleteMapping(path = "/{monitoringOperatorId}")
    @ApiOperation(value = "Deletes an existing monitoring operator entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the monitoring operator!"),
    		@ApiResponse(code = 404, message = "Monitoring operator or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("monitoringOperatorId") String monitoringOperatorId) throws EntityNotFoundException, MissingPermissionException {
    	// Delete the monitoring operator (includes access-control)
    	userEntityService.deleteWithAccessControlCheck(monitoringOperatorRepository, monitoringOperatorId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}
