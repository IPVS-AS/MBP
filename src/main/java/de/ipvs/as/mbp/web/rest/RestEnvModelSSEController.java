package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.repository.EnvironmentModelRepository;
import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import de.ipvs.as.mbp.service.env_model.events.EnvironmentModelEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for server-sent events regarding environment models.
 */
@RestController
@RequestMapping(Constants.BASE_PATH)
@Api(tags = {"Environment model SSE"}, description = "Subscription of server-sent events regarding updates of an environment model")
public class RestEnvModelSSEController {

    @Autowired
    private EnvironmentModelRepository environmentModelRepository;

    @Autowired
    private EnvironmentModelEventService eventService;


    @GetMapping(value = "/env-models/{id}/subscribe")
    @ApiOperation(value = "Subscribes to server-sent events of an environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Environment model not found or not authorized"), @ApiResponse(code = 500, message = "Registration failed")})
    public ResponseEntity<SseEmitter> subscribe(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findById(modelID).get();

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Subscribe environment model and obtain SSE emitter
        SseEmitter emitter = eventService.subscribe(modelID);

        //Embed emitter in response
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }
}
