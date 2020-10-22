package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.MonitoringAdapterRepository;
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
 * REST Controller for managing {@link MonitoringAdapter}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/monitoring-adapters")
@Api(tags = { "Monitoring Adapters" })
public class RestMonitoringAdapterController {
	
    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing monitoring adapter entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Monitoring monitoring adapter or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<MonitoringAdapter>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding monitoring adapters (includes access-control)
    	List<MonitoringAdapter> monitoringAdapters = userEntityService.getPageWithAccessControlCheck(monitoringAdapterRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(monitoringAdapters, selfLink, pageable));
    }
    
    @GetMapping(path = "/{monitoringAdapterId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing monitoring adapter entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the monitoring adapter!"),
    		@ApiResponse(code = 404, message = "Monitoring adapter or requesting user not found!") })
    public ResponseEntity<EntityModel<MonitoringAdapter>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("monitoringAdapterId") String monitoringAdapterId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
    	// Retrieve the corresponding monitoring adapter (includes access-control)
    	MonitoringAdapter monitoringAdapter = userEntityService.getForIdWithAccessControlCheck(monitoringAdapterRepository, monitoringAdapterId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(monitoringAdapter));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing monitoring adapter entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Monitoring adapter already exists!") })
    public ResponseEntity<EntityModel<MonitoringAdapter>> create(@PathVariable("monitoringAdapterId") String monitoringAdapterId, @ApiParam(value = "Page parameters", required = true) Pageable pageable, @RequestBody MonitoringAdapter monitoringAdapter) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save monitoring adapter in the database
    	MonitoringAdapter createdMonitoringAdapter = userEntityService.create(monitoringAdapterRepository, monitoringAdapter);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdMonitoringAdapter));
    }
    
    @DeleteMapping(path = "/{monitoringAdapterId}")
    @ApiOperation(value = "Deletes an existing monitoring adapter entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the monitoring adapter!"),
    		@ApiResponse(code = 404, message = "Monitoring adapter or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("monitoringAdapterId") String monitoringAdapterId) throws EntityNotFoundException, MissingPermissionException {
    	// Delete the monitoring adapter (includes access-control) 
    	userEntityService.deleteWithAccessControlCheck(monitoringAdapterRepository, monitoringAdapterId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}
