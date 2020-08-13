package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.ACPolicyRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.citopt.connde.util.C;
import org.citopt.connde.util.Pages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for managing {@link Device devices}.
 * 
 * @author Jakob Benz
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/policies")
@Api(tags = {"Access-Control Policies"})
public class RestACPolicyController {
	
	@Autowired
	private ACPolicyRepository policyRepository;

    @Autowired
    private UserRepository userRepository;
    
    
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing policies owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the entity!") })
    public ResponseEntity<PagedModel<EntityModel<ACPolicy>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	// Retrieve requesting user from the database (if it can be identified and is present)
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve all policies owned by the user (no paging yet)
//    	List<ACPolicy> policiesOwnedByUser = policyRepository.findByOwner(user.getId(), Pages.ALL);
    	List<ACPolicy> policiesOwnedByUser = policyRepository.findAll();
    	
    	// Extract requested page from all devices
    	List<ACPolicy> page = Pages.page(policiesOwnedByUser, pageable);
    	
    	// Add self link to every device
    	List<EntityModel<ACPolicy>> policyEntityModels = page.stream().map(this::policyToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(PagedModel.of(policyEntityModels, Pages.metaDataOf(pageable, policyEntityModels.size()), C.listOf(link)));
    }
    
//    @GetMapping(path = "/{policyId}", produces = "application/hal+json")
//    @ApiOperation(value = "Retrieves an existing policy identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
//    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the policy!"), @ApiResponse(code = 404, message = "Policy not found!") })
//    public ResponseEntity<EntityModel<ACPolicy>> one(@PathVariable("policyId") String policyId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
//    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
//    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
//    	
//    	// Retrieve the requested device from the database (if it exists)
//    	Optional<ACPolicy> deviceOptional = policyRepository.findById(policyId);
//    	if (!deviceOptional.isPresent()) {
//    		return ResponseEntity.notFound().build();
//    	}
//    	ACPolicy policy = deviceOptional.get();
//    	
//    	// Check whether the requesting user is the owner of the policy
//    	if (!policy.getOwner().getId().equals(user.getId())) {
//    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//    	}
//    			
//    	// Add self link to device
//    	EntityModel<ACPolicy> policyEntityModel = policyToEntityModel(policy);
//    	
//    	return ResponseEntity.ok(policyEntityModel);
//    }
    
    private EntityModel<ACPolicy> policyToEntityModel(ACPolicy policy) {
    	return EntityModel.of(policy).add(linkTo(getClass()).slash(policy.getId()).withSelfRel());
    }

}
