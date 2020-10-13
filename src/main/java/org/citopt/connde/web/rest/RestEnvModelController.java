package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.error.EntityAlreadyExistsException;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.EnvironmentModelParseException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.EnvironmentModelRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.env_model.EntityState;
import org.citopt.connde.service.env_model.EnvironmentModelService;
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
 * REST Controller for deployment related REST requests.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/env-models")
@Api(tags = { "Environment models" })
public class RestEnvModelController {

	@Autowired
	private EnvironmentModelRepository environmentModelRepository;

	@Autowired
	private EnvironmentModelService environmentModelService;
	
	@Autowired
	private UserEntityService userEntityService;
	
	
	@GetMapping(produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing environment model entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Environment model or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<EnvironmentModel>>> all(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) {
		// Parse the access-request information
		ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);
		
    	// Retrieve the corresponding environment models (includes access-control)
    	List<EnvironmentModel> environmentModels = userEntityService.getPageWithPolicyCheck(environmentModelRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(environmentModels, selfLink, pageable));
    }
    
    @GetMapping(path = "/{id}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing environment model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the environment model!"),
    		@ApiResponse(code = 404, message = "Environment model or requesting user not found!") })
    public ResponseEntity<EntityModel<EnvironmentModel>> one(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("id") String environmentModelId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException {
    	// Retrieve the corresponding environment model (includes access-control)
    	EnvironmentModel environmentModel = userEntityService.getForIdWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(environmentModel));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing environment model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Environment model already exists!") })
    public ResponseEntity<EntityModel<EnvironmentModel>> create(@RequestBody EnvironmentModel adapter) throws EntityAlreadyExistsException {
    	// Check whether a environment model with the same name already exists in the database
    	userEntityService.requireUniqueName(environmentModelRepository, adapter.getName());

    	// Save environment model in the database
    	EnvironmentModel createdEnvironmentModel = environmentModelRepository.save(adapter);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(createdEnvironmentModel));
    }
    
    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Deletes an existing environment model entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to delete the environment model!"),
    		@ApiResponse(code = 404, message = "Environment model or requesting user not found!") })
    public ResponseEntity<Void> delete(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
    		@PathVariable("id") String environmentModelId) throws EntityNotFoundException {
    	// Delete the environment model (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessRequest.valueOf(accessRequestHeader));
    	return ResponseEntity.noContent().build();
    }
	
	@GetMapping(value = "/{id}/states")
	@ApiOperation(value = "Retrieves the states of all registered entities of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the environment model!"),
			@ApiResponse(code = 404, message = "Environment model or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while retrieving the states!") })
	public ResponseEntity<Map<String, EntityState>> getEntityStates(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, EnvironmentModelParseException {
		// Retrieve the corresponding environment model (includes access-control)
    	EnvironmentModel environmentModel = userEntityService.getForIdWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

		// Determine entity states
    	environmentModelService.determineEntityStates(environmentModel);
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/{id}/register")
	@ApiOperation(value = "Registers the entities of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to register the entities of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while registering the entites!") })
	public ResponseEntity<Void> registerEntities(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
		doAction(environmentModelService::registerComponents, environmentModelId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/{id}/deploy")
	@ApiOperation(value = "Deploys the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to deploy the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while deploying the components") })
	public ResponseEntity<Void> deployComponents(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
		doAction(environmentModelService::deployComponents, environmentModelId, ACAccessType.DEPLOY, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/{id}/undeploy")
	@ApiOperation(value = "Undeploys the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to undeploy the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while undeploying the components") })
	public ResponseEntity<Void> undeployComponents(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
		doAction(environmentModelService::undeployComponents, environmentModelId, ACAccessType.UNDEPLOY, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/{id}/start")
	@ApiOperation(value = "Starts the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to start the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while starting the components") })
	public ResponseEntity<Void> startComponents(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
		doAction(environmentModelService::startComponents, environmentModelId, ACAccessType.START, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok().build();
	}

	@PostMapping(value = "/{id}/stop")
	@ApiOperation(value = "Stops the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to stop the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while stopping the components") })
	public ResponseEntity<Void> stopComponents(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
    	doAction(environmentModelService::stopComponents, environmentModelId, ACAccessType.STOP, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok().build();
	}
	
	private void doAction(Consumer<EnvironmentModel> action, String environmentModelId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
		// Retrieve the corresponding environment model (includes access-control)
    	EnvironmentModel environmentModel = userEntityService.getForIdWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, accessRequest);
    	
    	// Check permission
    	userEntityService.requirePermission(environmentModel, accessType, accessRequest);
    	
    	// Execute action
		action.accept(environmentModel);
	}
}
