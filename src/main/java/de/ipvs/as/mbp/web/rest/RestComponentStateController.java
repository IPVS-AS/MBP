package de.ipvs.as.mbp.web.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.ComponentRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.UserEntityService;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.service.deployment.ComponentState;
import de.ipvs.as.mbp.web.rest.helper.DeploymentWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
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
@Api(tags = { "Component state" })
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
	 * Retrieves the deployment status for all actuators available for the requesting user. 
	 */
	@GetMapping("/actuators/state")
	@ApiOperation(value = "Retrieves the component state of all actuators for which the user is authorized", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!") })
	public ResponseEntity<Map<String, ComponentState>> getStatesAllActuators(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader) {
		return ResponseEntity.ok(getStatesAllComponents(actuatorRepository, ACAccessRequest.valueOf(accessRequestHeader)));
	}

	/**
	 * Retrieves the deployment status for all sensors available for the requesting user.
	 */
	@GetMapping("/sensors/state")
	@ApiOperation(value = "Retrieves the component state of all sensors for which the user is authorized", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success") })
	public ResponseEntity<Map<String, ComponentState>> getStatesAllSensors(@RequestHeader("X-MBP-Access-Request") String accessRequestHeader) {
		return ResponseEntity.ok(getStatesAllComponents(sensorRepository, ACAccessRequest.valueOf(accessRequestHeader)));
	}

	/**
	 * Retrieves the deployment status for a certain actuator.
	 *
	 * @param actuatorId the id of the {@link Actuator}.
	 * @throws EntityNotFoundException
	 * @throws MissingPermissionException
	 */
	@GetMapping("/actuators/state/{id}")
	@ApiOperation(value = "Retrieves the component state for an actuator", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 401, message = "Not authorized to access the actuator"),
			@ApiResponse(code = 404, message = "Actuator not found") })
	public ResponseEntity<EntityModel<ComponentState>> getActuatorState(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the actuator", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId) throws EntityNotFoundException, MissingPermissionException {
		return ResponseEntity.ok(getComponentState(actuatorId, actuatorRepository, ACAccessRequest.valueOf(accessRequestHeader)));
	}

	/**
	 * Retrieves the deployment status for a certain sensor.
	 *
	 * @param sensorId the id of the {@link Sensor}.
	 * @throws EntityNotFoundException 
	 * @throws MissingPermissionException 
	 */
	@GetMapping("/sensors/state/{id}")
	@ApiOperation(value = "Retrieves the component state for a sensor", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success"),
			@ApiResponse(code = 401, message = "Not authorized to access the sensor"),
			@ApiResponse(code = 404, message = "Sensor not found") })
	public ResponseEntity<EntityModel<ComponentState>> getSensorState(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the sensor", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId) throws EntityNotFoundException, MissingPermissionException {
		return ResponseEntity.ok(getComponentState(sensorId, sensorRepository, ACAccessRequest.valueOf(accessRequestHeader)));
	}

	private <C extends Component> Map<String, ComponentState> getStatesAllComponents(ComponentRepository<C> repository, ACAccessRequest accessRequest) {
		// Retrieve all components from the database (according to owner and policies)
		List<Component> componentList = userEntityService.getAllWithAccessControlCheck(repository, ACAccessType.READ, accessRequest)
				.stream()
				.map(entity -> (Component) entity)
				.collect(Collectors.toList());

		// Determine the states of each component
		return deploymentWrapper.getStatesAllComponents(componentList);
	}

	private <C extends Component> EntityModel<ComponentState> getComponentState(String componentId, ComponentRepository<C> repository, ACAccessRequest accessRequest) throws EntityNotFoundException, MissingPermissionException {
		// Retrieve component from the database
		Component component = userEntityService.getForIdWithAccessControlCheck(repository, componentId, ACAccessType.READ, accessRequest);

		// Determine component state
		// TODO: Entity model really required here???
		return new EntityModel<ComponentState>(deploymentWrapper.getComponentState(component));
	}
}
