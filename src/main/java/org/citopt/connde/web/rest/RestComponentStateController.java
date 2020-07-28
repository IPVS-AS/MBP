package org.citopt.connde.web.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for requests related to the deployment state of components.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Component state"}, description = "Retrieval of component states")
public class RestComponentStateController {
    @Autowired
    private DeploymentWrapper deploymentWrapper;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Responds with the deployment state for all actuators in the actuator repository as a map.
     *
     * @return A map (actuator id -> actuator state) that contains the state of each actuator
     */
    @GetMapping("/actuators/state")
    @ApiOperation(value = "Retrieves the component state of all actuators for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<Map<String, ComponentState>> getStatesAllActuators() {
        return getStatesAllComponents(actuatorRepository);
    }

    /**
     * Responds with the deployment state for all sensors in the sensor repository as a map.
     *
     * @return A map (sensor id -> sensor state) that contains the state of each sensor
     */
    @GetMapping("/sensors/state")
    @ApiOperation(value = "Retrieves the component state of all sensors for which the user is authorized", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success")})
    public ResponseEntity<Map<String, ComponentState>> getStatesAllSensors() {
        return getStatesAllComponents(sensorRepository);
    }

    /**
     * Responds with the availability state for a certain actuator.
     *
     * @param actuatorId The id of the actuator whose state is supposed to be retrieved
     * @return The deployment state of the actuator as plain string
     */
    @GetMapping("/actuators/state/{id}")
    @ApiOperation(value = "Retrieves the component state for an actuator", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the actuator"), @ApiResponse(code = 404, message = "Actuator not found")})
    public ResponseEntity<EntityModel<ComponentState>> getActuatorState(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId) {
        return getComponentState(actuatorId, actuatorRepository);
    }

    /**
     * Responds with the availability state for a certain sensor.
     *
     * @param sensorId The id of the sensor whose state is supposed to be retrieved
     * @return The deployment state of the sensor as plain string
     */
    @GetMapping("/sensors/state/{id}")
    @ApiOperation(value = "Retrieves the component state for a sensor", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access the sensor"), @ApiResponse(code = 404, message = "Sensor not found")})
    public ResponseEntity<EntityModel<ComponentState>> getSensorState(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId) {
        return getComponentState(sensorId, sensorRepository);
    }

    private <C extends Component> ResponseEntity<Map<String, ComponentState>> getStatesAllComponents(ComponentRepository<C> repository) {
        //Get all components
        List<Component> componentList = userEntityService.getUserEntitiesFromRepository(repository)
                .stream().map(entity -> (Component) entity).collect(Collectors.toList());

        //Get states for all components
        return deploymentWrapper.getStatesAllComponents(componentList);
    }

    private <C extends Component> ResponseEntity<EntityModel<ComponentState>> getComponentState(String componentId, ComponentRepository<C> repository) {
        //Retrieve component from repository
        Component component = (Component) repository.get(componentId).get();

        //Get component state
        return deploymentWrapper.getComponentState(component);
    }
}
