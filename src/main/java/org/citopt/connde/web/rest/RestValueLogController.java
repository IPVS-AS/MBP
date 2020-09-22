package org.citopt.connde.web.rest;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import javax.validation.Valid;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.UnitConverterService;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.util.S;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import springfox.documentation.annotations.ApiIgnore;

/**
 * REST Controller for retrieving value logs for certain components.
 * Furthermore, it provides features for converting the value log values to
 * desired units.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = { "Value logs" })
public class RestValueLogController {

	@Autowired
	private ActuatorRepository actuatorRepository;

	@Autowired
	private SensorRepository sensorRepository;

	@Autowired
	private UserEntityService userEntityService;

	@Autowired
	private UnitConverterService unitConverterService;

	@Autowired
	private MonitoringHelper monitoringHelper;
	
	@Autowired
	private ValueLogRepository valueLogRepository;
	

	@GetMapping("/actuators/{id}/valueLogs")
	@ApiOperation(value = "Retrieves a list of recorded actuator value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value logs of this actuator!"),
			@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
	public ResponseEntity<Page<ValueLog>> getActuatorValueLogs(
			@PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the actuator values", example = "°C", required = false) String unit,
			@Valid @RequestBody ACAccessRequest<?> accessRequest,
			@ApiParam(value = "The page configuration", required = true) Pageable pageable) {
		// Retrieve actuator from the database (includes access-control)
		Actuator actuator = userEntityService.getForIdWithPolicyCheck(actuatorRepository, actuatorId, ACAccessType.READ_VALUE_LOGS, accessRequest);

		// Retrieve value logs
		return ResponseEntity.ok(getValueLogs(actuator, unit, pageable));
	}

	@GetMapping("/sensors/{id}/valueLogs")
	@ApiOperation(value = "Retrieves a list of recorded sensor value log in a certain unit which fit onto a given page", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value logs of this sensor!"),
			@ApiResponse(code = 404, message = "Sensor or requesting user not found!") })
	public ResponseEntity<Page<ValueLog>> getSensorValueLogs(
			@PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the sensor values", example = "°C", required = false) String unit,
			@Valid @RequestBody ACAccessRequest<?> accessRequest,
			@ApiParam(value = "The page configuration", required = true) Pageable pageable) {
		// Retrieve actuator from the database (includes access-control)
		// TODO: Formerly, permission 'deploy' has been checked - i think it should be the 'read' permission
		Sensor sensor = userEntityService.getForIdWithPolicyCheck(sensorRepository, sensorId, ACAccessType.READ, accessRequest);

		// Retrieve value logs
		return ResponseEntity.ok(getValueLogs(sensor, unit, pageable));
	}

	@GetMapping("/monitoring/{deviceId}/valueLogs")
	@ApiOperation(value = "Retrieves a list of recorded monitoring value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value logs of this monitoring component!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<Page<ValueLog>> getMonitoringValueLogs(
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value logs", example = "°C", required = false) String unit,
			@Valid @RequestBody ACAccessRequest<?> accessRequest,
			@ApiParam(value = "The page configuration", required = true) Pageable pageable) {
		// Create new monitoring component from parameters
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);
		
		// Check monitoring permission
		userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.MONITOR, accessRequest);

		// Retrieve value logs
		return ResponseEntity.ok(getValueLogs(monitoringComponent, unit, pageable));
	}

	@DeleteMapping("/actuators/{id}/valueLogs")
	@ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to delete value logs of this actuator!"),
			@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
	@ApiIgnore("Currently not working")
	public ResponseEntity<Void> deleteActuatorValueLogs(
			@PathVariable(value = "id") String actuatorId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		// TODO: Does checking the 'delete' permission for the actuator make sense (actually the value logs are deleted, not the actuator) (formerly the 'deploy' permission had been checked)?
		// Check permission
		userEntityService.requirePermission(actuatorRepository, actuatorId, ACAccessType.DELETE, accessRequest);

		// Delete value logs of this actuator
		valueLogRepository.deleteByIdRef(actuatorId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/sensors/{id}/valueLogs")
	@ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to delete value logs of this sensor!"),
			@ApiResponse(code = 404, message = "Sensor or requesting user not found!") })
	@ApiIgnore("Currently not working")
	public ResponseEntity<Void> deleteSensorValueLogs(
			@PathVariable(value = "id") String sensorId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {
		// TODO: Does checking the 'delete' permission for the actuator make sense (actually the value logs are deleted, not the actuator) (formerly the 'deploy' permission had been checked)?
		// Check permission
		userEntityService.requirePermission(sensorRepository, sensorId, ACAccessType.DELETE, accessRequest);

		// Delete value logs of this sensor
		valueLogRepository.deleteByIdRef(sensorId);
		return ResponseEntity.noContent().build(); 
	}

	@DeleteMapping("/monitoring/{deviceId}/valueLogs")
	@ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to delete value logs of this minitoring component!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	@ApiIgnore("Currently not working")
	public ResponseEntity<Void> deleteMonitoringValueLogs(@PathVariable(value = "deviceId") String deviceId,
			@RequestParam("adapter") String monitoringAdapterId,
			@Valid @RequestBody ACAccessRequest<?> accessRequest) {

		// Create new monitoring component from parameters
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);
		
		// Check delete permission
		userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.DELETE, accessRequest);

	
		// Delete value logs of this sensor
		valueLogRepository.deleteByIdRef(monitoringComponent.getId());
		return ResponseEntity.noContent().build(); 
	}

	/**
	 * Retrieves the value logs for a given component and converts them to a given unit.
	 *
	 * @param component the {@link Component} the value logs should be retrieved for.
	 * @param unit the target unit as {@code String}.
	 * @param pageable the {@link Pageable} to configure the result set.
	 * @return the requested {@link Page} with the converted {@link ValueLog}s.
	 */
	private <C extends Component> Page<ValueLog> getValueLogs(C component, String unit, Pageable pageable) {
		// Retrieve the value logs from the database (already paged)
		Page<ValueLog> page = valueLogRepository.findAllByIdRef(component.getId(), pageable);

		// Convert value logs if required
		if (S.notEmpty(unit)) {
			// Parse unit
			Unit<? extends Quantity> targetUnit;
			try {
				targetUnit = Unit.valueOf(unit);
			} catch (Exception e) {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid unit!");
			}

			// Get source unit
			Unit<? extends Quantity> sourceUnit = component.getAdapter().getUnitObject();

			// Convert value logs using corresponding converter
			UnitConverter converter = sourceUnit.getConverterTo(targetUnit);
			for (ValueLog valueLog : page) {
				unitConverterService.convertValueLogValue(valueLog, converter);
			}
		}
		
		return page;
	}

}
