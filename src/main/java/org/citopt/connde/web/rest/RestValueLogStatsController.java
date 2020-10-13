package org.citopt.connde.web.rest;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.stats.ValueLogStatsService;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.citopt.connde.util.S;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * REST Controller for requests related to the value log stats of components.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = { "Value logs statistics" }, description = "Retrieval of statistics for recorded value logs")
public class RestValueLogStatsController {

	@Autowired
	private ActuatorRepository actuatorRepository;

	@Autowired
	private SensorRepository sensorRepository;

	@Autowired
	private ValueLogStatsService valueLogStatsService;

	@Autowired
	private MonitoringHelper monitoringHelper;
	
	@Autowired
	private UserEntityService userEntityService;
	

	/**
	 * Responds with the value log stats for a certain actuator.
	 *
	 * @param actuatorId The id of the actuator whose value log stats are supposed
	 *                   to be retrieved
	 * @param unit       A string specifying the desired unit of the value log stats
	 * @return The value log stats of the actuator
	 * @throws EntityNotFoundException 
	 */
	@GetMapping("/actuators/{id}/stats")
	@ApiOperation(value = "Retrieves a list of statistics for recorded actuator value logs in a certain unit", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value log statistics of this actuator!"),
			@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
	public ResponseEntity<ValueLogStats> getActuatorValueLogStats(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
			@RequestParam(value = "unit", required = false) String unit) throws EntityNotFoundException {
		// Retrieve actuator from the database (includes access-control)
		Actuator actuator = userEntityService.getForIdWithAccessControlCheck(actuatorRepository, actuatorId, ACAccessType.READ_VALUE_LOG_STATS, ACAccessRequest.valueOf(accessRequestHeader));

		// Calculate value log stats
		return ResponseEntity.ok(calculateValueLogStats(actuator, unit));
	}

	/**
	 * Responds with the value log stats for a certain sensor.
	 *
	 * @param sensorId The id of the sensor whose value log stats are supposed to be
	 *                 retrieved
	 * @param unit     A string specifying the desired unit of the value log stats
	 * @return The value log stats of the sensor
	 * @throws EntityNotFoundException 
	 */
	@GetMapping("/sensors/{id}/stats")
	@ApiOperation(value = "Retrieves a list of statistics for recorded sensor value logs in a certain unit", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value log statistics of this sensor!"),
			@ApiResponse(code = 404, message = "Sensor or requesting user not found!") })
	public ResponseEntity<ValueLogStats> getSensorValueLogStats(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
			@RequestParam(value = "unit", required = false) String unit) throws EntityNotFoundException {
		// Retrieve actuator from the database (includes access-control)
		Sensor sensor = userEntityService.getForIdWithAccessControlCheck(sensorRepository, sensorId, ACAccessType.READ_VALUE_LOG_STATS, ACAccessRequest.valueOf(accessRequestHeader));

		// Calculate value log stats
		return ResponseEntity.ok(calculateValueLogStats(sensor, unit));
	}

	/**
	 * Responds with the value log stats for a certain monitoring component.
	 *
	 * @param deviceId            The id of the device for which the stats are
	 *                            supposed to be retrieved
	 * @param monitoringAdapterId The id of the monitoring adapter for which the
	 *                            stats are supposed to be retrieved
	 * @param unit                A string specifying the desired unit of the value
	 *                            log stats
	 * @return The value log stats of the sensor
	 * @throws MissingPermissionException 
	 * @throws EntityNotFoundException 
	 */
	@GetMapping("/monitoring/{deviceId}/stats")
	@ApiOperation(value = "Retrieves a list of statistics for recorded monitoring value logs in a certain unit", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
		@ApiResponse(code = 400, message = "Invalid unit specification!"),
		@ApiResponse(code = 401, message = "Not authorized to access value log statistics of this monitoring component!"),
		@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<ValueLogStats> getMonitoringValueLogStats(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value log statistics", example = "°C", required = false) String unit) throws MissingPermissionException, EntityNotFoundException {
		// Create new monitoring component from parameters
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);
		
		// Check monitoring permission
		userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.MONITOR, ACAccessRequest.valueOf(accessRequestHeader));

		// Retrieve value log statistics
		return ResponseEntity.ok(calculateValueLogStats(monitoringComponent, unit));
	}

	/**
	 * Calculates the value log statistics for a give component and unit.
	 * 
	 * @param <C> the component type.
	 * @param component the {@link Component}.
	 * @param unitString the unit specification as {@code String}.
	 * @return the {@link ValueLogStats}.
	 */
	private <C extends Component> ValueLogStats calculateValueLogStats(C component, String unitString) {
		// Parse unit
		Unit<? extends Quantity> unit = null;
		if (S.notEmpty(unitString)) {
			try {
				unit = Unit.valueOf(unitString);
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid unit!");
			}
		}

		// Calculate stats
		return valueLogStatsService.calculateValueLogStats(component, unit);
	}
}
