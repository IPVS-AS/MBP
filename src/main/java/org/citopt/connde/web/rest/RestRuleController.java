package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.RuleActionRepository;
import org.citopt.connde.repository.RuleRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.rules.RuleEngine;
import org.citopt.connde.service.rules.RuleExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller that exposes methods for the purpose of managing rules.
 */
@RestController()
@RequestMapping(RestConfiguration.BASE_PATH + "/rules")
@Api(tags = { "Rules" })
public class RestRuleController {

    @Autowired
    private RuleActionRepository ruleActionRepository;

    @Autowired
    private RuleRepository ruleRepository;

    @Autowired
    private RuleEngine ruleEngine;

    @Autowired
    private RuleExecutor ruleExecutor;
    
    @Autowired
    private UserEntityService userEntityService;
    
    
    @GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing rule entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Rule or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<Rule>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding rules (includes access-control)
    	List<Rule> rules = userEntityService.getPageWithAccessControlCheck(ruleRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(rules, selfLink, pageable));
    }
    
    @GetMapping(path = "/{ruleId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the rule!"),
    		@ApiResponse(code = 404, message = "Rule or requesting user not found!") })
    public ResponseEntity<EntityModel<Rule>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("ruleId") String ruleId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException {
    	// Retrieve the corresponding rule (includes access-control)
    	Rule rule = userEntityService.getForIdWithAccessControlCheck(ruleRepository, ruleId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(rule));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 409, message = "Rule already exists!") })
    public ResponseEntity<EntityModel<Rule>> create(
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody Rule rule) throws EntityAlreadyExistsException, EntityNotFoundException {
    	// Save rule in the database
    	Rule createdRule = userEntityService.create(ruleRepository, rule);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdRule));
    }
    
    @DeleteMapping(path = "/{ruleId}")
    @ApiOperation(value = "Deletes an existing rule entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the rule!"),
    		@ApiResponse(code = 404, message = "Rule or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("ruleId") String ruleId) throws EntityNotFoundException {
    	// Delete the rule (includes access-control) 
    	userEntityService.deleteWithAccessControlCheck(ruleRepository, ruleId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
    
	@PostMapping(value = "/rules/enable/{id}")
	public ResponseEntity<Void> enableRule(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") String ruleId) throws MissingPermissionException, EntityNotFoundException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
		// Retrieve the corresponding rule (includes access-control)
		Rule rule = userEntityService.getForIdWithAccessControlCheck(ruleRepository, ruleId, ACAccessType.READ, accessRequest);
		
		// Check permission
		userEntityService.requirePermission(rule, ACAccessType.START, accessRequest);

		// Enable rule if necessary
		if (!rule.isEnabled() && !ruleEngine.enableRule(rule)) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Rule could not be enabled. Do all required components still exist?");
		}
		
		// TODO: Adjust frontend since response content changed
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/rules/disable/{id}")
	public ResponseEntity<Void> disableRule(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") String ruleId) throws EntityNotFoundException, MissingPermissionException {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
		// Retrieve the corresponding rule (includes access-control)
		Rule rule = userEntityService.getForIdWithAccessControlCheck(ruleRepository, ruleId, ACAccessType.READ, accessRequest);
		
		// Check permission
		userEntityService.requirePermission(rule, ACAccessType.STOP, accessRequest);

		// Enable rule if necessary
		if (rule.isEnabled()) {
			ruleEngine.disableRule(rule);
		}
		
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/rule-actions/test/{id}")
	public ResponseEntity<Boolean> testRuleAction(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") String actionId) throws EntityNotFoundException {
		// Retrieve the corresponding rule action (includes access-control)
		RuleAction ruleAction = userEntityService.getForIdWithAccessControlCheck(ruleActionRepository, actionId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

		// Test action
		boolean result = ruleExecutor.testRuleAction(ruleAction);

		// TODO: Adjust frontend since response content changed
		return ResponseEntity.ok(result);
	}
}
