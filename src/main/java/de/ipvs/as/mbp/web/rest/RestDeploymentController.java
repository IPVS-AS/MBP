package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;
import de.ipvs.as.mbp.error.DeploymentException;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.ComponentRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import io.swagger.annotations.*;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * REST Controller for deployment related REST requests.
 */
@RestController
@RequestMapping(Constants.BASE_PATH)
@Api(tags = {"Deployment"})
public class RestDeploymentController implements RepresentationModelProcessor<EntityModel<?>> {

    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private UserEntityService userEntityService;


    @PostMapping(value = "/start/actuator/{id}")
    @ApiOperation(value = "Starts an actuator with optional deployment parameters", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid parameters provided!"),
            @ApiResponse(code = 401, message = "Not authorized to start the actuator!"),
            @ApiResponse(code = 404, message = "Device, actuator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Starting the actuator failed due to an unexpected error!")})
    public ResponseEntity<Void> startActuator(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id,
            @Valid @RequestBody @ApiParam(value = "The list of parameters to use") List<ParameterInstance> parameters) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.startComponent(getComponentWithPermissionCheck(actuatorRepository, id, ACAccessType.START, ACAccessRequest.valueOf(accessRequestHeader)), parameters);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/start/sensor/{id}")
    @ApiOperation(value = "Starts a sensor with optional deployment parameters", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid parameters provided!"),
            @ApiResponse(code = 401, message = "Not authorized to start the sensor!"),
            @ApiResponse(code = 404, message = "Device, sensor or requesting user not found!"),
            @ApiResponse(code = 500, message = "Starting the sensor failed due to an unexpected error!")})
    public ResponseEntity<Void> startSensor(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id,
            @Valid @RequestBody @ApiParam(value = "The list of parameters to use") List<ParameterInstance> parameters) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.startComponent(getComponentWithPermissionCheck(sensorRepository, id, ACAccessType.START, ACAccessRequest.valueOf(accessRequestHeader)), parameters);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop/actuator/{id}")
    @ApiOperation(value = "Stops a running actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to stop the actuator!"),
            @ApiResponse(code = 404, message = "Device, actuator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Stopping the actuator failed due to an unexpected error!")})
    public ResponseEntity<Void> stopActuator(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.stopComponent(getComponentWithPermissionCheck(actuatorRepository, id, ACAccessType.STOP, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stop/sensor/{id}")
    @ApiOperation(value = "Stops a running sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to stop the sensor!"),
            @ApiResponse(code = 404, message = "Device, sensor or requesting user not found!"),
            @ApiResponse(code = 500, message = "Stopping the sensor failed due to an unexpected error!")})
    public ResponseEntity<Void> stopSensor(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.stopComponent(getComponentWithPermissionCheck(sensorRepository, id, ACAccessType.STOP, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deploy/actuator/{id}")
    @ApiOperation(value = "Checks if an actuator is currently deployed", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the actuator!"),
            @ApiResponse(code = 404, message = "Device, actuator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Check failed due to an unexpected error!")})
    public ResponseEntity<Void> isRunningActuator(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.isComponentRunning(getComponentWithPermissionCheck(actuatorRepository, id, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/deploy/sensor/{id}")
    @ApiOperation(value = "Checks if a sensor is currently deployed", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to access the sensor!"),
            @ApiResponse(code = 404, message = "Device, sensor or requesting user not found!"),
            @ApiResponse(code = 500, message = "Check failed due to an unexpected error!")})
    public ResponseEntity<Void> isRunningSensor(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.isComponentRunning(getComponentWithPermissionCheck(sensorRepository, id, ACAccessType.READ, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deploy/actuator/{id}")
    @ApiOperation(value = "Deploys an actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to deploy the actuator!"),
            @ApiResponse(code = 404, message = "Device, actuator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Check failed due to an unexpected error!")})
    public ResponseEntity<Void> deployActuator(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.deployComponent(getComponentWithPermissionCheck(actuatorRepository, id, ACAccessType.DEPLOY, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/deploy/sensor/{id}")
    @ApiOperation(value = "Deploys a sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 201, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to deploy the sensor!"),
            @ApiResponse(code = 404, message = "Device, sensor or requesting user not found!"),
            @ApiResponse(code = 500, message = "Check failed due to an unexpected error!")})
    public ResponseEntity<Void> deploySensor(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.deployComponent(getComponentWithPermissionCheck(sensorRepository, id, ACAccessType.DEPLOY, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deploy/actuator/{id}")
    @ApiOperation(value = "Undeploys an actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Not authorized to undeploy the actuator!"),
            @ApiResponse(code = 404, message = "Device, actuator or requesting user not found!"),
            @ApiResponse(code = 500, message = "Check failed due to an unexpected error!")})
    public ResponseEntity<Void> undeployActuator(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.undeployComponent(getComponentWithPermissionCheck(actuatorRepository, id, ACAccessType.UNDEPLOY, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deploy/sensor/{id}")
    @ApiOperation(value = "Undeploys a sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"),
            @ApiResponse(code = 401, message = "Not authorized to undeploy the sensor!"),
            @ApiResponse(code = 404, message = "Device, sensor or requesting user not found!"),
            @ApiResponse(code = 500, message = "Check failed due to an unexpected error!")})
    public ResponseEntity<Void> undeploySensor(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String id) throws EntityNotFoundException, MissingPermissionException, DeploymentException {
        deploymentWrapper.undeployComponent(getComponentWithPermissionCheck(sensorRepository, id, ACAccessType.UNDEPLOY, ACAccessRequest.valueOf(accessRequestHeader)));
        return ResponseEntity.ok().build();
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

    private <C extends Component> C getComponentWithPermissionCheck(ComponentRepository<C> repository, String id, ACAccessType accessType, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve component from the database
        C component = userEntityService.getForIdWithAccessControlCheck(repository, id, ACAccessType.READ, accessRequest);

        // Check permission
        userEntityService.requirePermission(component, accessType, accessRequest);

        // Everything check out -> return component
        return component;
    }


    @Override
    public EntityModel<?> process(EntityModel<?> model) {
        return model;
    }
}
