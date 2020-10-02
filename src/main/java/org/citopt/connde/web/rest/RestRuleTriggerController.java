package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.repository.RuleTriggerRepository;
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
 * REST Controller that exposes methods for the purpose of managing rule triggers.
 */
@RestController()
@RequestMapping(RestConfiguration.BASE_PATH + "/rule-triggers")
@Api(tags = { "Rule Triggers" })
public class RestRuleTriggerController {

    @Autowired
    private RuleTriggerRepository ruleTriggerRepository;

    @Autowired
    private UserEntityService userEntityService;
    
    
    @GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing rule trigger entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Rule trigger or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<RuleTrigger>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding rule triggers (includes access-control)
    	List<RuleTrigger> ruleTriggers = userEntityService.getPageWithPolicyCheck(ruleTriggerRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(ruleTriggers, selfLink, pageable));
    }
    
    @GetMapping(path = "/{ruleTriggerId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule trigger entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the rule trigger!"),
    		@ApiResponse(code = 404, message = "Rule trigger or requesting user not found!") })
    public ResponseEntity<EntityModel<RuleTrigger>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("ruleTriggerId") String ruleTriggerId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException {
    	// Retrieve the corresponding rule trigger (includes access-control)
    	RuleTrigger ruleTrigger = userEntityService.getForIdWithPolicyCheck(ruleTriggerRepository, ruleTriggerId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(ruleTrigger));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule trigger entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Rule trigger already exists!") })
    public ResponseEntity<EntityModel<RuleTrigger>> create(
    		@PathVariable("ruleTriggerId") String ruleTriggerId, @ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody RuleTrigger ruleTrigger) throws EntityAlreadyExistsException {
    	// Check whether a rule trigger with the same name already exists in the database
    	userEntityService.requireUniqueName(ruleTriggerRepository, ruleTrigger.getName());

    	// Save rule trigger in the database
    	RuleTrigger createdRuleTrigger = ruleTriggerRepository.save(ruleTrigger);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdRuleTrigger));
    }
    
    @DeleteMapping(path = "/{ruleTriggerId}")
    @ApiOperation(value = "Deletes an existing rule trigger entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the rule trigger!"),
    		@ApiResponse(code = 404, message = "Rule trigger or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("ruleTriggerId") String ruleTriggerId) throws EntityNotFoundException {
    	// Delete the rule trigger (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(ruleTriggerRepository, ruleTriggerId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
}