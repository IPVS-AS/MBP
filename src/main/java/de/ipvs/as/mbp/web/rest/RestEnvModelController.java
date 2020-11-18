package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityAlreadyExistsException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.EnvironmentModelParseException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.EnvironmentModelRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import io.swagger.annotations.*;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import de.ipvs.as.mbp.service.env_model.EntityState;
import de.ipvs.as.mbp.service.env_model.EnvironmentModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

/**
 * REST Controller for deployment related REST requests.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH + "/env-models")
@Api(tags = {"Environment models"})
public class RestEnvModelController {

    @Autowired
    private EnvironmentModelRepository environmentModelRepository;

    @Autowired
    private EnvironmentModelService environmentModelService;

    @Autowired
    private UserEntityService userEntityService;


    @GetMapping(produces = "application/hal+json")
    @ApiOperation(value = "Retrieves all existing environment model entities available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Environment model or requesting user not found!")})
    public ResponseEntity<PagedModel<EntityModel<EnvironmentModel>>> all(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) {
        // Parse the access-request information
        ACAccessRequest accessRequest = ACAccessRequest.valueOf(accessRequestHeader);

        // Retrieve the corresponding environment models (includes access-control)
        List<EnvironmentModel> environmentModels = userEntityService.getPageWithAccessControlCheck(environmentModelRepository, ACAccessType.READ, accessRequest, pageable);

        // Create self link
        Link selfLink = linkTo(methodOn(getClass()).all(accessRequestHeader, pageable)).withSelfRel();

        return ResponseEntity.ok(userEntityService.entitiesToPagedModel(environmentModels, selfLink, pageable));
    }

    @GetMapping(path = "/{id}", produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing environment model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the environment model!"),
            @ApiResponse(code = 404, message = "Environment model or requesting user not found!")})
    public ResponseEntity<EntityModel<EnvironmentModel>> one(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("id") String environmentModelId,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding environment model (includes access-control)
        EnvironmentModel environmentModel = userEntityService.getForIdWithAccessControlCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(userEntityService.entityToEntityModel(environmentModel));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Retrieves an existing environment model entity identified by its id if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 409, message = "Environment model already exists!")})
    public ResponseEntity<EntityModel<EnvironmentModel>> create(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @ApiParam(value = "Page parameters", required = true) Pageable pageable,
            @RequestBody EnvironmentModel adapter) throws EntityAlreadyExistsException, EntityNotFoundException {
        // Save environment model in the database
        EnvironmentModel createdEnvironmentModel = userEntityService.create(environmentModelRepository, adapter);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdEnvironmentModel));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/hal+json")
    @ApiOperation(value = "Updates an existing environment model entity if it's available for the requesting entity.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 404, message = "Environment model not found!")})
    public ResponseEntity<EntityModel<EnvironmentModel>> update(
            @PathVariable(name = "id") String id,
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @RequestBody EnvironmentModel environmentModel) throws EntityNotFoundException, MissingPermissionException {
    	// Check permission (and whether environment model exists)
    	userEntityService.requirePermission(environmentModelRepository, id, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));

        // Fix entity ID
        environmentModel.setId(id);

        // Save updated environment model to the database
        EnvironmentModel createdEnvironmentModel = environmentModelRepository.save(environmentModel);
        return ResponseEntity.ok(userEntityService.entityToEntityModel(createdEnvironmentModel));
    }

    @DeleteMapping(path = "/{id}")
    @ApiOperation(value = "Deletes an existing environment model entity identified by its id if it's available for the requesting entity.")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete the environment model!"),
            @ApiResponse(code = 404, message = "Environment model or requesting user not found!")})
    public ResponseEntity<Void> delete(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable("id") String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
        // Delete the environment model (includes access-control)
        userEntityService.deleteWithAccessControlCheck(environmentModelRepository, environmentModelId, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/states")
    @ApiOperation(value = "Retrieves the states of all registered entities of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the environment model!"),
            @ApiResponse(code = 404, message = "Environment model or requesting user not found!"),
            @ApiResponse(code = 500, message = "An error occurred while retrieving the states!")})
    public ResponseEntity<Map<String, EntityState>> getEntityStates(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, EnvironmentModelParseException, MissingPermissionException {
        // Retrieve the corresponding environment model (includes access-control)
        EnvironmentModel environmentModel = userEntityService.getForIdWithAccessControlCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader));

        // Determine entity states
        environmentModelService.determineEntityStates(environmentModel);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/register")
    @ApiOperation(value = "Registers the entities of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to register the entities of the environment model!"),
            @ApiResponse(code = 404, message = "Environment model or requesting user not found!"),
            @ApiResponse(code = 500, message = "An error occurred while registering the entites!")})
    public ResponseEntity<Void> registerEntities(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
        doAction(environmentModelService::registerComponents, environmentModelId, ACAccessType.UPDATE, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/deploy")
    @ApiOperation(value = "Deploys the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to deploy the components of the environment model!"),
            @ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
            @ApiResponse(code = 500, message = "An error occurred while deploying the components")})
    public ResponseEntity<Void> deployComponents(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
        doAction(environmentModelService::deployComponents, environmentModelId, ACAccessType.DEPLOY, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/undeploy")
    @ApiOperation(value = "Undeploys the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to undeploy the components of the environment model!"),
            @ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
            @ApiResponse(code = 500, message = "An error occurred while undeploying the components")})
    public ResponseEntity<Void> undeployComponents(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
        doAction(environmentModelService::undeployComponents, environmentModelId, ACAccessType.UNDEPLOY, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/start")
    @ApiOperation(value = "Starts the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to start the components of the environment model!"),
            @ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
            @ApiResponse(code = 500, message = "An error occurred while starting the components")})
    public ResponseEntity<Void> startComponents(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
        doAction(environmentModelService::startComponents, environmentModelId, ACAccessType.START, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/{id}/stop")
    @ApiOperation(value = "Stops the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to stop the components of the environment model!"),
            @ApiResponse(code = 404, message = "Environment model, components  or requesting user not found!"),
            @ApiResponse(code = 500, message = "An error occurred while stopping the components")})
    public ResponseEntity<Void> stopComponents(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String environmentModelId) throws EntityNotFoundException, MissingPermissionException {
        doAction(environmentModelService::stopComponents, environmentModelId, ACAccessType.STOP, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok().build();
    }

    private void doAction(Consumer<EnvironmentModel> action, String environmentModelId, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve the corresponding environment model (includes access-control)
        EnvironmentModel environmentModel = userEntityService.getForIdWithAccessControlCheck(environmentModelRepository, environmentModelId, ACAccessType.READ, accessRequest);

        // Check permission
        userEntityService.requirePermission(environmentModel, accessType, accessRequest);

        // Execute action
        action.accept(environmentModel);
    }
}
