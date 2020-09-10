package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAbstractCondition;
import org.citopt.connde.domain.access_control.dto.ACConditionRequestDTO;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.ACConditionRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.citopt.connde.service.access_control.ACConditionService;
import org.citopt.connde.util.C;
import org.citopt.connde.util.Pages;
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
@RequestMapping(RestConfiguration.BASE_PATH + "/policy-conditions")
@Api(tags = {"Access-Control Policy Conditions"})
public class RestACConditionController {
	
	@Autowired
	private ACConditionService conditionService;
	
	@Autowired
	private ACConditionRepository conditionRepository;
	
	@Autowired
	private UserRepository userRepository;
	

	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing conditions owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACAbstractCondition>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	return ResponseEntity.ok(conditionsToPagedModel(conditionRepository.findByOwner(user.getId(), pageable), pageable));
    }
    
    @GetMapping(path = "/{conditionId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing condition identified by its id if available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the condition!"), @ApiResponse(code = 404, message = "Condition or requesting user not found!") })
    public ResponseEntity<EntityModel<ACAbstractCondition>> one(@PathVariable("conditionId") String conditionId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	return ResponseEntity.ok(conditionToEntityModel(conditionService.getForIdAndOwner(conditionId, user.getId())));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new condition.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 201, message = "Condition successfully created!"), @ApiResponse(code = 404, message = "Requesting user not found!"), @ApiResponse(code = 409, message = "Condition name already exists!") })
    public ResponseEntity<EntityModel<ACAbstractCondition>> create(@Valid @RequestBody ACConditionRequestDTO requestDto, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	return ResponseEntity.status(HttpStatus.CREATED).body(conditionToEntityModel(conditionService.create(requestDto, user.getId()))); 
    }
    
    @DeleteMapping(path = "/{conditionId}")
    @ApiOperation(value = "Deletes an existing condition.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 204, message = "Condition successfully deleted!"), @ApiResponse(code = 404, message = "Requesting user or condition not found!") })
    public ResponseEntity<Void> delete(@PathVariable("conditionId") String conditionId) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	conditionService.delete(conditionId, user.getId());
    	return ResponseEntity.noContent().build();
    }
    
    private EntityModel<ACAbstractCondition> conditionToEntityModel(ACAbstractCondition condition) {
    	return new EntityModel<>(condition, linkTo(getClass()).slash(condition.getId()).withSelfRel());
    }
    
    private PagedModel<EntityModel<ACAbstractCondition>> conditionsToPagedModel(List<ACAbstractCondition> conditions, Pageable pageable) {
    	// Extract requested page from all conditions
    	List<ACAbstractCondition> page = Pages.page(conditions, pageable);
    	
    	// Add self link to every condition
    	List<EntityModel<ACAbstractCondition>> conditionEntityModels = page.stream().map(this::conditionToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable)).withSelfRel();
    	
    	// Create and return paged model
    	return new PagedModel<>(conditionEntityModels, Pages.metaDataOf(pageable, conditionEntityModels.size()), C.listOf(link));
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	// ------------------------------------------------------------------------------------------------------------------------------------------
	
	@GetMapping(path = "/1")
    public ResponseEntity<Void> test1() {
//		IACCondition sc1 = new ACSimpleCondition<Double>("Simple condition 1", ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<Double>(1D), new ACConditionSimpleValueArgument<Double>(1D));
//		IACCondition sc2 = new ACSimpleCondition<Double>("Simple condition 2", ACArgumentFunction.EQUALS, new ACConditionSimpleValueArgument<Double>(2D), new ACConditionSimpleValueArgument<Double>(2D));
//		
//		IACCondition cc1 = new ACCompositeCondition("Composite condition 1", ACLogicalOperator.AND, C.listOf(sc1, sc2));
//		
//		conditionRepository.save(sc1);
//		conditionRepository.save(sc2);
//		conditionRepository.save(cc1);
		
		// - - -
		
//		ACAbstractCondition c = conditionRepository.findAll().get(0);
//		ACPolicy p = new ACPolicy("Test Policy 1", 1, C.listOf(ACAccessType.READ), c, new ArrayList<>(), userRepository.findOneByUsername("admin").get());
//		p = policyRepository.save(p);
		
    	return ResponseEntity.ok().build();
    }
	
}
