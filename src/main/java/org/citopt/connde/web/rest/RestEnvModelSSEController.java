package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.repository.EnvironmentModelRepository;
import org.citopt.connde.service.env_model.EnvironmentModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalTime;
import java.util.concurrent.Executors;

/**
 * REST Controller for server-sent events regarding environment models.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Environment model SSE"}, description = "Subscription of server-sent events regarding updates of an environment model")
public class RestEnvModelSSEController {

    @Autowired
    private EnvironmentModelRepository environmentModelRepository;

    @Autowired
    private EnvironmentModelService environmentModelService;


    @GetMapping(value = "/env-models/{id}/sse")
    @ApiOperation(value = "Subscribes to server-sent events of an environment model", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 404, message = "Environment model not found or not authorized"), @ApiResponse(code = 500, message = "Registration failed")})
    public ResponseEntity<SseEmitter> subscribe(@PathVariable(value = "id") @ApiParam(value = "ID of the environment model", example = "5c97dc2583aeb6078c5ab672", required = true) String modelID) {
        //Get environment model
        EnvironmentModel model = environmentModelRepository.findOne(modelID);

        //Check if model could be found
        if (model == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        SseEmitter emitter = new SseEmitter(2 * 60 * 1000L);

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                for (int i = 0; true; i++) {
                    SseEmitter.SseEventBuilder event = SseEmitter.event()
                            .data("SSE MVC - " + LocalTime.now().toString())
                            .id(String.valueOf(i))
                            .name("sse event - mvc");
                    emitter.send(event);
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });

        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }
}
