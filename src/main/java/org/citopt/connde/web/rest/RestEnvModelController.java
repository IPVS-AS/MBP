package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.repository.EnvironmentModelRepository;
import org.citopt.connde.service.env_model.EntityState;
import org.citopt.connde.service.env_model.EnvironmentModelService;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for deployment related REST requests.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Environment model"}, description = "Actions for environment models")
public class RestEnvModelController {

    @Autowired
    private EnvironmentModelRepository environmentModelRepository;

    @Autowired
    private EnvironmentModelService environmentModelService;

    @GetMapping(value = "/env-models/{id}/states")
    @ApiOperation(value = "Retrieves the states of all registered entities of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the entities of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "State retrieval failed")})
    public ResponseEntity<Map<String, EntityState>> getEntityStates(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Get entity states
        Map<String, EntityState> entityStates = environmentModelService.determineEntityStates(model);

        //Return response
        return new ResponseEntity<>(entityStates, HttpStatus.OK);
    }

    @PostMapping(value = "/env-models/{id}/register")
    @ApiOperation(value = "Registers the entities of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to register the entities of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Registration failed")})
    public ResponseEntity<ActionResponse> registerEntities(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(new ActionResponse(false, "Model could not be found"), HttpStatus.NOT_FOUND);
        }

        //Call service for component registration
        ActionResponse response = environmentModelService.registerComponents(model);

        //Check for success and return response
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/env-models/{id}/deploy")
    @ApiOperation(value = "Deploys the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to deploy the components of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Deployment failed")})
    public ResponseEntity<ActionResponse> deployComponents(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(new ActionResponse(false, "Model could not be found"), HttpStatus.NOT_FOUND);
        }

        //Call service for component deployment
        ActionResponse response = environmentModelService.deployComponents(model);

        //Check for success and return response
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/env-models/{id}/undeploy")
    @ApiOperation(value = "Undeploys the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to undeploy the components of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Undeployment failed")})
    public ResponseEntity<ActionResponse> undeployComponents(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(new ActionResponse(false, "Model could not be found"), HttpStatus.NOT_FOUND);
        }

        //Call service for component undeployment
        ActionResponse response = environmentModelService.undeployComponents(model);

        //Check for success and return response
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/env-models/{id}/start")
    @ApiOperation(value = "Starts the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to start the components of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Starting failed")})
    public ResponseEntity<ActionResponse> startComponents(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(new ActionResponse(false, "Model could not be found"), HttpStatus.NOT_FOUND);
        }

        //Call service for component start
        ActionResponse response = environmentModelService.startComponents(model);

        //Check for success and return response
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/env-models/{id}/stop")
    @ApiOperation(value = "Stops the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to stop the components of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Stopping failed")})
    public ResponseEntity<ActionResponse> stopComponents(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(new ActionResponse(false, "Model could not be found"), HttpStatus.NOT_FOUND);
        }

        //Call service for component stop
        ActionResponse response = environmentModelService.stopComponents(model);

        //Check for success and return response
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
