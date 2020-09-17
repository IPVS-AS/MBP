package org.citopt.connde.web.rest;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.repository.EnvironmentModelRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.env_model.EntityState;
import org.citopt.connde.service.env_model.EnvironmentModelService;
import org.citopt.connde.web.rest.response.ActionResponse;
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
	
	
	@GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
	@ApiOperation(value = "Retrieves all existing environment model entities available for the requesting entity.", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 404, message = "Environment model or requesting user not found!") })
    public ResponseEntity<PagedModel<EntityModel<EnvironmentModel>>> all(
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Retrieve the corresponding environment models (includes access-control)
    	List<EnvironmentModel> environmentModels = userEntityService.getPageWithPolicyCheck(environmentModelRepository, ACAccessType.READ, accessRequest, pageable);
    	
    	// Create self link
    	Link selfLink = linkTo(methodOn(getClass()).all(pageable, accessRequest)).withSelfRel();
    	
    	return ResponseEntity.ok(userEntityService.entitiesToPagedModel(environmentModels, selfLink, pageable));
    }
    
    @GetMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing environment model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 401, message = "Not authorized to access the environment model!"),
    		@ApiResponse(code = 404, message = "Environment model or requesting user not found!") })
    public ResponseEntity<EntityModel<EnvironmentModel>> one(
    		@PathVariable("id") String environmentModelId,
    		@ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Retrieve the corresponding environment model (includes access-control)
    	EnvironmentModel environmentModel = userEntityService.getForIdWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, accessRequest);
    	return ResponseEntity.ok(userEntityService.entityToEntityModel(environmentModel));
    }
    
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing environment model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
    		@ApiResponse(code = 409, message = "Environment model already exists!") })
    public ResponseEntity<EntityModel<EnvironmentModel>> create(
    		@PathVariable("environmentModelId") String environmentModelId, @ApiParam(value = "Page parameters", required = true) Pageable pageable,
    		@RequestBody EnvironmentModel adapter) {
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
    		@PathVariable("id") String environmentModelId,
    		@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	// Delete the environment model (includes access-control) 
    	userEntityService.deleteWithPolicyCheck(environmentModelRepository, environmentModelId, accessRequest);
    	return ResponseEntity.noContent().build();
    }
	
	@GetMapping(value = "/{id}/states")
	@ApiOperation(value = "Retrieves the states of all registered entities of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to access the environment model!"),
			@ApiResponse(code = 404, message = "Environment model or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while retrieving the states!") })
	public ResponseEntity<Map<String, EntityState>> getEntityStates(
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		// Retrieve the corresponding environment model (includes access-control)
    	EnvironmentModel environmentModel = userEntityService.getForIdWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, accessRequest);

		// Determine entity states
		return ResponseEntity.ok(environmentModelService.determineEntityStates(environmentModel));
	}

	@PostMapping(value = "/{id}/register")
	@ApiOperation(value = "Registers the entities of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to register the entities of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while registering the entites!") })
	public ResponseEntity<ActionResponse> registerEntities(
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		ActionResponse response = doAction(environmentModelService::registerComponents, environmentModelId, ACAccessType.UPDATE, accessRequest);
		return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@PostMapping(value = "/{id}/deploy")
	@ApiOperation(value = "Deploys the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to deploy the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while deploying the components") })
	public ResponseEntity<ActionResponse> deployComponents(
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		ActionResponse response = doAction(environmentModelService::deployComponents, environmentModelId, ACAccessType.DEPLOY, accessRequest);
		return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@PostMapping(value = "/{id}/undeploy")
	@ApiOperation(value = "Undeploys the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to undeploy the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while undeploying the components") })
	public ResponseEntity<ActionResponse> undeployComponents(
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		ActionResponse response = doAction(environmentModelService::undeployComponents, environmentModelId, ACAccessType.UNDEPLOY, accessRequest);
		return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@PostMapping(value = "/{id}/start")
	@ApiOperation(value = "Starts the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to start the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while starting the components") })
	public ResponseEntity<ActionResponse> startComponents(
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		ActionResponse response = doAction(environmentModelService::startComponents, environmentModelId, ACAccessType.START, accessRequest);
		return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}

	@PostMapping(value = "/{id}/stop")
	@ApiOperation(value = "Stops the components of a given environment model", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to stop the components of the environment model!"),
			@ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
			@ApiResponse(code = 500, message = "An error occurred while stopping the components") })
	public ResponseEntity<ActionResponse> stopComponents(
			@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
    	ActionResponse response = doAction(environmentModelService::stopComponents, environmentModelId, ACAccessType.STOP, accessRequest);
		return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR).body(response);
	}
	
	private ActionResponse doAction(Function<EnvironmentModel, ActionResponse> action, String environmentModelId, ACAccessType accessType, ACAccessRequest<?> accessRequest) {
		// Retrieve the corresponding environment model (includes access-control)
    	EnvironmentModel environmentModel = userEntityService.getForIdWithPolicyCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, accessRequest);
    	
    	// Check permission
    	userEntityService.requirePermission(environmentModel, accessType, accessRequest);
    	
    	// Execute action
		return action.apply(environmentModel);
	}
}
