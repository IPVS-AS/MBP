package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.parameters.ParameterInstance;
import org.citopt.connde.domain.adapter.parameters.ParameterType;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.web.rest.helper.DeploymentWrapper;
import org.citopt.connde.web.rest.response.ActionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * REST Controller for deployment related REST requests.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Deployment"}, description = "Deployment management for actuators and sensors")
public class RestDeploymentController implements ResourceProcessor<RepositoryLinksResource> {

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @RequestMapping(value = "/start/actuator/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "Starts an actuator with optional deployment parameters", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 400, message = "Invalid deployment parameters provided"), @ApiResponse(code = 403, message = "Not authorized to start the actuator"), @ApiResponse(code = 404, message = "Actuator not found"), @ApiResponse(code = 500, message = "Starting attempt failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> startActuator(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id,
                                                        @RequestBody @ApiParam(value = "List of deployment parameter instances to use") List<ParameterInstance> parameters) {
        return startComponent(id, actuatorRepository, parameters);
    }

    @RequestMapping(value = "/start/sensor/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "Starts a sensor with optional deployment parameters", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 400, message = "Invalid deployment parameters provided"), @ApiResponse(code = 403, message = "Not authorized to start the sensor"), @ApiResponse(code = 404, message = "Sensor not found"), @ApiResponse(code = 500, message = "Starting attempt failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> startSensor(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id,
                                                      @RequestBody @ApiParam(value = "List of deployment parameter instances to use") List<ParameterInstance> parameters) {
        return startComponent(id, sensorRepository, parameters);
    }

    @RequestMapping(value = "/stop/actuator/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "Stops a running actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to stop the actuator"), @ApiResponse(code = 404, message = "Actuator not found"), @ApiResponse(code = 500, message = "Stopping attempt failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> stopActuator(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return stopComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/stop/sensor/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "Stops a running sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to stop the sensor"), @ApiResponse(code = 404, message = "Sensor not found"), @ApiResponse(code = 500, message = "Stopping attempt failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> stopSensor(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return stopComponent(id, sensorRepository);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Checks if an actuator is currently deployed", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the actuator"), @ApiResponse(code = 404, message = "Actuator not found"), @ApiResponse(code = 500, message = "Check failed due to an unexpected I/O error")})
    public ResponseEntity<Boolean> isRunningActuator(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return isRunningComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Checks if a sensor is currently deployed", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the sensor"), @ApiResponse(code = 404, message = "Sensor not found"), @ApiResponse(code = 500, message = "Check failed due to an unexpected I/O error")})
    public ResponseEntity<Boolean> isRunningSensor(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return isRunningComponent(id, sensorRepository);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "Deploys an actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to deploy the actuator"), @ApiResponse(code = 404, message = "Actuator not found"), @ApiResponse(code = 500, message = "Deployment failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> deployActuator(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return deployComponent(id, actuatorRepository);
    }

    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.POST)
    @ApiOperation(value = "Deploys a sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to deploy the sensor"), @ApiResponse(code = 404, message = "Sensor not found"), @ApiResponse(code = 500, message = "Deployment failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> deploySensor(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return deployComponent(id, sensorRepository);
    }

    @RequestMapping(value = "/deploy/actuator/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Undeploys an actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to undeploy the actuator"), @ApiResponse(code = 404, message = "Actuator not found"), @ApiResponse(code = 500, message = "Undeployment failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> undeployActuator(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return undeployComponent(id, actuatorRepository);
    }


    @RequestMapping(value = "/deploy/sensor/{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Undeploys a sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to undeploy the sensor"), @ApiResponse(code = 404, message = "Sensor not found"), @ApiResponse(code = 500, message = "Undeployment failed due to an unexpected I/O error")})
    public ResponseEntity<ActionResponse> undeploySensor(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) {
        return undeployComponent(id, sensorRepository);
    }

    private ResponseEntity<Boolean> isRunningComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Check if running
        return deploymentWrapper.isComponentRunning(component);
    }

    private ResponseEntity<ActionResponse> startComponent(String id, ComponentRepository repository, List<ParameterInstance> parameterInstances) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Sanitize parameters list
        if (parameterInstances == null) {
            parameterInstances = new ArrayList<>();
        }

        //Start component
        return deploymentWrapper.startComponent(component, parameterInstances);
    }

    private ResponseEntity<ActionResponse> stopComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Stop component
        return deploymentWrapper.stopComponent(component);
    }

    private ResponseEntity<ActionResponse> deployComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Do deployment
        return deploymentWrapper.deployComponent(component);
    }

    private ResponseEntity<ActionResponse> undeployComponent(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Do undeployment
        return deploymentWrapper.undeployComponent(component);
    }

    @RequestMapping(value = "/adapter/parameter-types", method = RequestMethod.GET)
    @ApiOperation(value = "Returns a list of all available parameter types", produces = "application/hal+json")
    @ApiResponse(code = 200, message = "Success")
    public ResponseEntity<List<ParameterType>> getAllParameterTypes() {
        //Get all enum objects as list
        List<ParameterType> parameterList = Arrays.asList(ParameterType.values());
        return new ResponseEntity<>(parameterList, HttpStatus.OK);
    }

    @GetMapping("/time")
    @ApiOperation(value = "Returns the current server time", notes = "Format of the returned timestamp: yyyy-MM-dd HH:mm:ss", produces = "application/hal+json")
    @ApiResponses(@ApiResponse(code = 200, message = "Success"))
    public ResponseEntity<String> serverDatetime() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return new ResponseEntity<>(strDate, HttpStatus.OK);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource resource) {
        return resource;
    }
}
