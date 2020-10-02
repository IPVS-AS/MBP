package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.repository.AdapterRepository;
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
 * REST Controller for managing {@link Adapter}s.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/adapters")
@Api(tags = { "Adapters" })
public class RestAdapterController {
	
    @Autowired
    private AdapterRepository adapterRepository;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing adapter entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Adapter or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<Adapter>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding adapters (includes access-control)
    	List<Adapter> adapters = userEntityService.getPageWithPolicyCheck(adapterRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(adapters, selfLink, pageable));
    }
    
    @GetMapping(path = "/{adapterId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing adapter entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the adapter!"),
    		@ApiResponse(code = 404, message = "Adapter or requesting user not found!") })
    public ResponseEntity<EntityModel<Adapter>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("adapterId") String adapterId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException {
    	// Retrieve the corresponding adapter (includes access-control)
    	Adapter adapter = userEntityService.getForIdWithPolicyCheck(adapterRepository, adapterId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(adapter));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing adapter entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Adapter already exists!") })
    public ResponseEntity<EntityModel<Adapter>> create(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("adapterId") String adapterId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody Adapter adapter) throws EntityAlreadyExistsException {
    	// Check whether a adapter with the same name already exists in the database
    	userEntityService.requireUniqueName(adapterRepository, adapter.getName());

    	// Save adapter in the database
    	Adapter createdAdapter = adapterRepository.save(adapter);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdAdapter));
    }
    
    @DeleteMapping(path = "/{adapterId}")
    @ApiOperation(value = "Deletes an existing adapter entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the adapter!"),
    		@ApiResponse(code = 404, message = "Adapter or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("adapterId") String adapterId) throws EntityNotFoundException {
    	// Delete the adapter (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(adapterRepository, adapterId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}
