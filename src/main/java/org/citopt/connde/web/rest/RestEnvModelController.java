package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.repository.EnvironmentModelRepository;
import org.citopt.connde.service.env_model.EnvironmentModelService;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @PostMapping(value = "/env-models/{id}/register")
    @ApiOperation(value = "Registers the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to register the components of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Registration failed")})
    public ResponseEntity<ActionResponse> registerComponents(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
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
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/env-models/{id}/deploy")
    @ApiOperation(value = "Deploys the components of a given environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to deploy the components of this environment model"), @ApiResponse(code = 404, message = "Environment model not found"), @ApiResponse(code = 500, message = "Deployment failed")})
    public ResponseEntity<ActionResponse> deployrComponents(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(new ActionResponse(false, "Model could not be found"), HttpStatus.NOT_FOUND);
        }

        //Call service for component registration
        ActionResponse response = environmentModelService.deployComponents(model);

        //Check for success and return response
        if (response.isSuccess()) {
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
