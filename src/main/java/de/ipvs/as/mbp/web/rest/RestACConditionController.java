package de.ipvs.as.mbp.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.EntityStillInUseException;
import de.ipvs.as.mbp.error.MissingOwnerPrivilegesException;
import de.ipvs.as.mbp.service.user.UserService;
import de.ipvs.as.mbp.util.C;
import de.ipvs.as.mbp.util.Pages;
import de.ipvs.as.mbp.domain.access_control.ACAbstractCondition;
import de.ipvs.as.mbp.domain.access_control.ACAttributeKey;
import de.ipvs.as.mbp.domain.access_control.dto.ACConditionRequestDTO;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.access_control.ACConditionService;
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
	private UserService userService;
	

	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing conditions owned by the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 404, message = "Requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<ACAbstractCondition>>> all(@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(conditionsToPagedModel(conditionService.getAllForOwner(user.getId(), pageable), pageable));
    }
    
    @GetMapping(path = "/{conditionId}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing condition identified by its id if available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"), @ApiResponse(code = 401, message = "Not authorized to access the condition!"), @ApiResponse(code = 404, message = "Condition or requesting user not found!") })
    public ResponseEntity<EntityModel<ACAbstractCondition>> one(@PathVariable("conditionId") String conditionId, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	return ResponseEntity.ok(conditionToEntityModel(conditionService.getForIdAndOwner(conditionId, user.getId())));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Creates a new condition.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 201, message = "Condition successfully created!"), @ApiResponse(code = 404, message = "Requesting user not found!"), @ApiResponse(code = 409, message = "Condition name already exists!") })
    public ResponseEntity<EntityModel<ACAbstractCondition>> create(@Valid @RequestBody ACConditionRequestDTO requestDto, @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityAlreadyExistsException {
    	User user = userService.getLoggedInUser();
    	return ResponseEntity.status(HttpStatus.CREATED).body(conditionToEntityModel(conditionService.create(requestDto, user.getId()))); 
    }
    
    @DeleteMapping(path = "/{conditionId}")
    @ApiOperation(value = "Deletes an existing condition.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 204, message = "Condition successfully deleted!"), @ApiResponse(code = 404, message = "Requesting user or condition not found!") })
    public ResponseEntity<Void> delete(@PathVariable("conditionId") String conditionId) throws EntityNotFoundException, EntityStillInUseException, MissingOwnerPrivilegesException {
    	User user = userService.getLoggedInUser();
    	conditionService.delete(conditionId, user.getId());
    	return ResponseEntity.noContent().build();
    }
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiOperation(value = "Retrieves all supported condition attribute keys.", produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!") })
    public ResponseEntity<List<ACAttributeKey>> attributeKeys() {
    	List<ACAttributeKey> attributeKeys = new ArrayList<>();
    	for (ACAttributeKey key : ACAttributeKey.values()) {
    		attributeKeys.add(key);
    	}
    	return ResponseEntity.ok(attributeKeys);
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
    
}
