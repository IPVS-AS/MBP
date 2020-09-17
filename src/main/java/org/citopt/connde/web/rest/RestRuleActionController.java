package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.repository.RuleActionRepository;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller that exposes methods for the purpose of managing rule actions.
 */
@RestController()
@RequestMapping(RestConfiguration.BASE_PATH + "/rule-actions")
@Api(tags = { "Rule Actions" })
public class RestRuleActionController {

    @Autowired
    private RuleActionRepository ruleActionRepository;

    @Autowired
    private UserEntityService userEntityService;
    
    
    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing rule trigger entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Rule action or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<RuleAction>>> all(
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Retrieve the corresponding rule actions (includes access-control)
    	List<RuleAction> ruleActions = userEntityService.getPageWithPolicyCheck(ruleActionRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(pageable, accessRequest)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(ruleActions, selfLink, pageable));
    }
    
    @GetMapping(path = "/{ruleActionId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule action entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the rule action!"),
    		@ApiResponse(code = 404, message = "Rule action or requesting user not found!") })
    public ResponseEntity<EntityModel<RuleAction>> one(
    		@PathVariable("ruleActionId") String ruleActionId, @ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Retrieve the corresponding rule action (includes access-control)
    	RuleAction ruleAction = userEntityService.getForIdWithPolicyCheck(ruleActionRepository, ruleActionId, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(ruleAction));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing rule action entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Rule action already exists!") })
    public ResponseEntity<EntityModel<RuleAction>> create(
    		@PathVariable("ruleActionId") String ruleActionId, @ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody RuleAction ruleAction) {
    	// Check whether a rule action with the same name already exists in the database
    	userEntityService.requireUniqueName(ruleActionRepository, ruleAction.getName());

    	// Save rule action in the database
    	RuleAction createdRuleAction = ruleActionRepository.save(ruleAction);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdRuleAction));
    }
    
    @DeleteMapping(path = "/{ruleActionId}")
    @ApiOperation(value = "Deletes an existing rule action entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the rule action!"),
    		@ApiResponse(code = 404, message = "Rule action or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@PathVariable("ruleActionId") String ruleActionId,
    		@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Delete the rule action (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(ruleActionRepository, ruleActionId, accessRequest);
    	return ResponseEntity.noContent().build();
    }
    
}