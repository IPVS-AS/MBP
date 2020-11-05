package org.citopt.connde.web.rest;

import java.io.File;
import java.io.FileWriter;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.access_control.ACAbstractEffect;
import org.citopt.connde.domain.access_control.ACAccessRequest;
import org.citopt.connde.domain.access_control.ACAccessType;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MBPException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.UnitConverterService;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.access_control.ACEffectService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
	private ValueLogRepository valueLogRepository;

	@Autowired
	private UserEntityService userEntityService;

	@Autowired
	private UnitConverterService unitConverterService;
	
	@Autowired
	private ACEffectService effectService;

	@Autowired
	private MonitoringHelper monitoringHelper;
	

	@GetMapping("/actuators/{id}/valueLogs")
	@ApiOperation(value = "Retrieves a list of recorded actuator value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value logs of this actuator!"),
			@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
	public ResponseEntity<Page<ValueLog>> getActuatorValueLogs(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the actuator values", example = "°C", required = false) String unit,
			@ApiParam(value = "The page configuration", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
		// Retrieve actuator from the database (includes access-control)
		Actuator actuator = userEntityService.getForId(actuatorRepository, actuatorId);
		
		// Retrieve value logs
		Page<ValueLog> valueLogs = getValueLogs(actuator, unit, pageable, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok(valueLogs);
	}

	@GetMapping("/sensors/{id}/valueLogs")
	@ApiOperation(value = "Retrieves a list of recorded sensor value log in a certain unit which fit onto a given page", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value logs of this sensor!"),
			@ApiResponse(code = 404, message = "Sensor or requesting user not found!") })
	public ResponseEntity<Page<ValueLog>> getSensorValueLogs(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the sensor values", example = "°C", required = false) String unit,
			@ApiParam(value = "The page configuration", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
		// Retrieve actuator from the database (includes access-control)
		Sensor sensor = userEntityService.getForId(sensorRepository, sensorId);
		
		// Retrieve value logs
		Page<ValueLog> valueLogs = getValueLogs(sensor, unit, pageable, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok(valueLogs);
	}

	@GetMapping("/monitoring/{deviceId}/valueLogs")
	@ApiOperation(value = "Retrieves a list of recorded monitoring value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
	@ApiResponses({ @ApiResponse(code = 200, message = "Success!"),
			@ApiResponse(code = 400, message = "Invalid unit specification!"),
			@ApiResponse(code = 401, message = "Not authorized to access value logs of this monitoring component!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	public ResponseEntity<Page<ValueLog>> getMonitoringValueLogs(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
			@RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
			@RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value logs", example = "°C", required = false) String unit,
			@ApiParam(value = "The page configuration", required = true) Pageable pageable) throws MissingPermissionException, EntityNotFoundException {
		// Create new monitoring component from parameters
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);
		
		// Check permission
		userEntityService.requirePermission(monitoringComponent, ACAccessType.MONITOR, ACAccessRequest.valueOf(accessRequestHeader));
		
		// Retrieve value logs
		Page<ValueLog> valueLogs = getValueLogs(monitoringComponent, unit, pageable, ACAccessRequest.valueOf(accessRequestHeader));
		return ResponseEntity.ok(valueLogs);
	}

	@DeleteMapping("/actuators/{id}/valueLogs")
	@ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to delete value logs of this actuator!"),
			@ApiResponse(code = 404, message = "Actuator or requesting user not found!") })
	@ApiIgnore("Currently not working")
	public ResponseEntity<Void> deleteActuatorValueLogs(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") String actuatorId) throws EntityNotFoundException, MissingPermissionException {
		// Check permission
		userEntityService.requirePermission(actuatorRepository, actuatorId, ACAccessType.DELETE_VALUE_LOGS, ACAccessRequest.valueOf(accessRequestHeader));

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
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "id") String sensorId) throws EntityNotFoundException, MissingPermissionException {
		// Check permission
		userEntityService.requirePermission(sensorRepository, sensorId, ACAccessType.DELETE_VALUE_LOGS, ACAccessRequest.valueOf(accessRequestHeader));

		// Delete value logs of this sensor
		valueLogRepository.deleteByIdRef(sensorId);
		return ResponseEntity.noContent().build(); 
	}

	@DeleteMapping("/monitoring/{deviceId}/valueLogs")
	@ApiResponses({ @ApiResponse(code = 204, message = "Success!"),
			@ApiResponse(code = 401, message = "Not authorized to delete value logs of this minitoring component!"),
			@ApiResponse(code = 404, message = "Device, monitoring adapter or requesting user not found!") })
	@ApiIgnore("Currently not working")
	public ResponseEntity<Void> deleteMonitoringValueLogs(
    		@RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
			@PathVariable(value = "deviceId") String deviceId,
			@RequestParam("adapter") String monitoringAdapterId) throws MissingPermissionException, EntityNotFoundException {

		// Create new monitoring component from parameters
		MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);
		
		// Check delete permission
		userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.DELETE_VALUE_LOGS, ACAccessRequest.valueOf(accessRequestHeader));

	
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
	 * @throws MissingPermissionException 
	 * @throws EntityNotFoundException 
	 */
	private <C extends Component> Page<ValueLog> getValueLogs(C component, String unit, Pageable pageable, ACAccessRequest accessRequest) throws MissingPermissionException, EntityNotFoundException {
		ACPolicy policy = null;
		if (!userEntityService.checkAdmin() && !userEntityService.checkOwner(component)) {			
			// Check permission (if access is granted, the policy that grants access is returned)
			policy = userEntityService.getFirstPolicyGrantingAccess(component, ACAccessType.READ_VALUE_LOGS, accessRequest)
					.orElseThrow(() -> new MissingPermissionException("Component", component.getId(), ACAccessType.READ_VALUE_LOGS));
		}
		
		// Retrieve the value logs from the database (already paged)
		Page<ValueLog> page = valueLogRepository.findAllByIdRef(component.getId(), pageable);

		// Convert value logs if required
		if (S.notEmpty(unit)) {
			// Parse unit
			Unit<? extends Quantity> targetUnit;
			try {
				targetUnit = Unit.valueOf(unit);
			} catch (Exception e) {
				throw new MBPException(HttpStatus.BAD_REQUEST, "Invalid unit!");
			}

			// Get source unit
			Unit<? extends Quantity> sourceUnit = component.getAdapter().getUnitObject();

			// Convert value logs using corresponding converter
			UnitConverter converter = sourceUnit.getConverterTo(targetUnit);
			for (ValueLog valueLog : page) {
				unitConverterService.convertValueLogValue(valueLog, converter);
			}
		}
		
		String filename = "/Users/jakob/Desktop/log3.txt";
		if (new File(filename).exists()) {
			new File(filename).delete();
		}
		try {
			FileWriter fw = new FileWriter(filename);
			try { fw.write("1: " + (policy == null) + "\n"); } catch (Exception e) { e.printStackTrace(); }
			try { fw.write("2: " + (policy.getEffectId() == null) + "\n"); } catch (Exception e) { e.printStackTrace(); }
			
			
			// - - -
			// Keep this
			for (ValueLog l : page) {
				try { fw.write("V1: " + l.getValue() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			}
			
			// Apply effect (constraints)
			if (policy != null && policy.getEffectId() != null) {
				ACAbstractEffect effect = effectService.getForId(policy.getEffectId());
				try { fw.write("3: " + (effect == null) + "\n"); } catch (Exception e) { e.printStackTrace(); }
				page.forEach(effect::apply);
				for (ValueLog l : page) {
					try { fw.write("4: " + effect.apply(l) + "\n"); } catch (Exception e) { e.printStackTrace(); fw.write("4.1: " + e.getMessage() + "\n"); }
				}
			}
			// - - -
			
			for (ValueLog l : page) {
				try { fw.write("V3: " + l.getValue() + "\n"); } catch (Exception e) { e.printStackTrace(); }
			}
			
			fw.flush();
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		
		return page;
	}

}
