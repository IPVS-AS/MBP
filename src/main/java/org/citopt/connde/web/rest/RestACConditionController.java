package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;
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
@RequestMapping(RestConfiguration.BASE_PATH + "/policy-condition")
@Api(tags = {"Access-Control Policy Conditions"})
public class RestACConditionController {
	
	@Autowired
	private ACConditionRepository conditionRepository;
	
	@Autowired
	private UserRepository userRepository;
	

	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing conditions owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACAbstractCondition>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	// Retrieve requesting user from the database (if it can be identified and is present)
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve all policies owned by the user (no paging yet)
    	List<ACAbstractCondition> conditionsOwnedByUser = conditionRepository.findByOwner(user.getId(), Pages.ALL);
    	
    	// Extract requested page from all policies
    	List<ACAbstractCondition> page = Pages.page(conditionsOwnedByUser, pageable);
    	
    	// Add self link to every policy
    	List<EntityModel<ACAbstractCondition>> conditionEntityModels = page.stream().map(this::conditionToEntityModel).collect(Collectors.toList());
    	
    	// Create self link
    	Link link = linkTo(methodOn(getClass()).all(pageable)).withSelfRel();
    	return ResponseEntity.ok(new PagedModel<>(conditionEntityModels, Pages.metaDataOf(pageable, conditionEntityModels.size()), C.listOf(link)));
//    	return ResponseEntity.ok(PagedModel.of(conditionEntityModels, Pages.metaDataOf(pageable, conditionEntityModels.size()), C.listOf(link)));
    }
    
    @GetMapping(path = "/{conditionId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing condition identified by its id if available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the condition!"), @ApiResponse(code = 404, message = "Condition or requesting user not found!") })
    public ResponseEntity<EntityModel<ACAbstractCondition>> one(@PathVariable("conditionId") String conditionId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Retrieve the requested condition from the database (if it exists)
    	Optional<ACAbstractCondition> conditionOptional = conditionRepository.findById(conditionId);
    	if (!conditionOptional.isPresent()) {
    		return ResponseEntity.notFound().build();
    	}
    	ACAbstractCondition condition = conditionOptional.get();
    	
    	// Check whether the requesting user is the owner of the condition
    	if (!condition.getOwner().getId().equals(user.getId())) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    			
    	// Add self link to condition
    	EntityModel<ACAbstractCondition> conditionEntityModel = conditionToEntityModel(condition);
    	
    	return ResponseEntity.ok(conditionEntityModel);
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new condition.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 201, message = "Condition successfully created!"), @ApiResponse(code = 404, message = "Requesting user not found!"), @ApiResponse(code = 409, message = "Condition name already exists!") })
    public ResponseEntity<EntityModel<ACAbstractCondition>> create(@Valid @RequestBody ACConditionRequestDTO requestDto, @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));
    	
    	// Check whether a policy with the same name exists already
    	if (conditionRepository.existsByName(requestDto.getName())) {
    		return ResponseEntity.status(HttpStatus.CONFLICT).build();
    	}
    	
    	// Create new condition and save it in the database
    	ACAbstractCondition condition = (ACAbstractCondition) ACAbstractCondition.forJQBOutput(requestDto.getCondition())
    			.setName(requestDto.getName())
    			.setDescription(requestDto.getDescription())
    			.setOwner(user);
    	condition = conditionRepository.save(condition);
    	
    	// Add self link to policy
    	EntityModel<ACAbstractCondition> conditionEntityModel = conditionToEntityModel(condition);
    	
    	return ResponseEntity.status(HttpStatus.CREATED).body(conditionEntityModel); 
    }
    
    @DeleteMapping(path = "/{conditionId}")
    @ApiOperation(value = "Deletes an existing condition.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 204, message = "Condition successfully deleted!"), @ApiResponse(code = 404, message = "Requesting user or condition not found!") })
    public ResponseEntity<Void> delete(@PathVariable("conditionId") String conditionId) {
    	User user = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername())
    			.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Requesting user not found!"));

    	// Retrieve the condition to delete from the database (if it exists)
    	Optional<ACAbstractCondition> conditionOptional = conditionRepository.findById(conditionId);
    	if (!conditionOptional.isPresent()) {
    		return ResponseEntity.notFound().build();
    	}
    	ACAbstractCondition condition = conditionOptional.get();
    	
    	// Check whether the requesting user is the owner of the condition
    	if (!condition.getOwner().getId().equals(user.getId())) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	
    	return ResponseEntity.noContent().build();
    }
    
    private EntityModel<ACAbstractCondition> conditionToEntityModel(ACAbstractCondition condition) {
    	return new EntityModel<>(condition, linkTo(getClass()).slash(condition.getId()).withSelfRel());
//    	return EntityModel.of(condition).add(linkTo(getClass()).slash(condition.getId()).withSelfRel());
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
