package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.RestConfiguration;
import de.ipvs.as.mbp.domain.access_control.ACAbstractEffect;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.access_control.ACPolicy;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.repository.ValueLogRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.UnitConverterService;
import de.ipvs.as.mbp.service.access_control.ACEffectService;
import de.ipvs.as.mbp.service.discovery.deployment.DynamicDeployableComponent;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.util.S;
import de.ipvs.as.mbp.web.rest.helper.MonitoringHelper;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for retrieving value logs for certain components.
 * Furthermore, it provides features for converting the value log values to
 * desired units.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Value logs"})
public class RestValueLogController {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ValueLogRepository valueLogRepository;

    @Autowired
    private DynamicDeploymentRepository dynamicDeploymentRepository;

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
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value logs of this actuator!"),
            @ApiResponse(code = 404, message = "Actuator or requesting user not found!")})
    public ResponseEntity<Page<ValueLog>> getActuatorValueLogs(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
            @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the actuator values", example = "°C", required = false) String unit,
            @RequestParam(value = "startTime", required = false) @ApiParam(value = "The desired start time for filtering in time", example = "°C", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) @ApiParam(value = "The desired end time for filtering in time", example = "°C", required = false) Long endTime,
            @ApiParam(value = "The page configuration", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve actuator from the database (includes access-control)
        Actuator actuator = userEntityService.getForId(actuatorRepository, actuatorId);

        // Retrieve value logs
        Page<ValueLog> valueLogs = getValueLogs(actuator, unit, startTime, endTime, pageable, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(valueLogs);
    }

    @GetMapping("/sensors/{id}/valueLogs")
    @ApiOperation(value = "Retrieves a list of recorded sensor value log in a certain unit which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value logs of this sensor!"),
            @ApiResponse(code = 404, message = "Sensor or requesting user not found!")})
    public ResponseEntity<Page<ValueLog>> getSensorValueLogs(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
            @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the sensor values", example = "°C", required = false) String unit,
            @RequestParam(value = "startTime", required = false) @ApiParam(value = "The desired start time for filtering in time", example = "°C", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) @ApiParam(value = "The desired end time for filtering in time", example = "°C", required = false) Long endTime,
            @ApiParam(value = "The page configuration", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        //Retrieve sensor from the database (includes access-control)
        Sensor sensor = userEntityService.getForId(sensorRepository, sensorId);

        //Retrieve value logs
        Page<ValueLog> valueLogs = getValueLogs(sensor, unit, startTime, endTime, pageable, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(valueLogs);
    }

    @GetMapping("/monitoring/{deviceId}/valueLogs")
    @ApiOperation(value = "Retrieves a list of recorded monitoring value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value logs of this monitoring component!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<Page<ValueLog>> getMonitoringValueLogs(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
            @RequestParam("monitoringOperatorId") @ApiParam(value = "ID of the monitoring operator to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringOperatorId,
            @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value logs", example = "°C", required = false) String unit,
            @RequestParam(value = "startTime", required = false) @ApiParam(value = "The desired start time for filtering in time", example = "°C", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) @ApiParam(value = "The desired end time for filtering in time", example = "°C", required = false) Long endTime,
            @ApiParam(value = "The page configuration", required = true) Pageable pageable) throws MissingPermissionException, EntityNotFoundException {
        // Create new monitoring component from parameters
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check permission
        userEntityService.requirePermission(monitoringComponent, ACAccessType.MONITOR, ACAccessRequest.valueOf(accessRequestHeader));

        // Retrieve value logs
        Page<ValueLog> valueLogs = getValueLogs(monitoringComponent, unit, startTime, endTime, pageable, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(valueLogs);
    }

    @GetMapping("/discovery/dynamic-deployments/{dynamicDeploymentId}/valueLogs")
    @ApiOperation(value = "Retrieves a list of dynamic deployment value logs in a certain unit, fitting onto a given page.", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value logs of this dynamic deployment!"),
            @ApiResponse(code = 404, message = "Dynamic deployment or requesting user not found!")})
    public ResponseEntity<Page<ValueLog>> getDynamicDeploymentValueLogs(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "dynamicDeploymentId") @ApiParam(value = "ID of the dynamic deployment to retrieve the value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String dynamicDeploymentId,
            @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the dynamic deployment values", example = "°C", required = false) String unit,
            @RequestParam(value = "startTime", required = false) @ApiParam(value = "The desired start time for filtering in time", example = "°C", required = false) Long startTime,
            @RequestParam(value = "endTime", required = false) @ApiParam(value = "The desired end time for filtering in time", example = "°C", required = false) Long endTime,
            @ApiParam(value = "The page configuration", required = true) Pageable pageable) throws EntityNotFoundException, MissingPermissionException {
        //Retrieve dynamic deployment from the database (includes access-control)
        DynamicDeployment dynamicDeployment = userEntityService.getForId(dynamicDeploymentRepository, dynamicDeploymentId);

        //Create deployable component from dynamic deployment
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicDeployment);

        // Retrieve value logs
        Page<ValueLog> valueLogs = getValueLogs(component, unit, startTime, endTime, pageable, ACAccessRequest.valueOf(accessRequestHeader));
        return ResponseEntity.ok(valueLogs);
    }

    @DeleteMapping("/actuators/{id}/valueLogs")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete value logs of this actuator!"),
            @ApiResponse(code = 404, message = "Actuator or requesting user not found!")})
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
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete value logs of this sensor!"),
            @ApiResponse(code = 404, message = "Sensor or requesting user not found!")})
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
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete value logs of this minitoring component!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<Void> deleteMonitoringValueLogs(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") String deviceId,
            @RequestParam("monitoringOperatorId") String monitoringOperatorId) throws MissingPermissionException, EntityNotFoundException {

        // Create new monitoring component from parameters
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check delete permission
        userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.DELETE_VALUE_LOGS, ACAccessRequest.valueOf(accessRequestHeader));


        // Delete value logs of this sensor
        valueLogRepository.deleteByIdRef(monitoringComponent.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/discovery/dynamic-deployments/{dynamicDeploymentId}/valueLogs")
    @ApiResponses({@ApiResponse(code = 204, message = "Success!"),
            @ApiResponse(code = 401, message = "Not authorized to delete value logs of this dynamic deployment!"),
            @ApiResponse(code = 404, message = "Dynamic deployment or requesting user not found!")})
    public ResponseEntity<Void> deleteDynamicDeploymentValueLogs(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "dynamicDeploymentId") String dynamicDeploymentId) throws EntityNotFoundException, MissingPermissionException {
        // Check permission
        userEntityService.requirePermission(dynamicDeploymentRepository, dynamicDeploymentId, ACAccessType.DELETE_VALUE_LOGS, ACAccessRequest.valueOf(accessRequestHeader));

        // Delete value logs of this dynamic deployment
        valueLogRepository.deleteByIdRef(dynamicDeploymentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves the value logs for a given component and an optional time window and converts them to a given unit.
     *
     * @param component The {@link Component} the value logs should be retrieved for.
     * @param unit      The target unit as {@code String}.
     * @param startTime The start time for filtering in time
     * @param endTime   The end time for filtering in time
     * @param pageable  The {@link Pageable} to configure the result set.
     * @return the requested {@link Page} with the converted {@link ValueLog}s.
     * @throws MissingPermissionException In case of missing permissions
     * @throws EntityNotFoundException    In case that the entity could not be found
     */
    private <C extends Component> Page<ValueLog> getValueLogs(C component, String unit, Long startTime, Long endTime, Pageable pageable, ACAccessRequest accessRequest) throws MissingPermissionException, EntityNotFoundException {
        ACPolicy policy = null;
        if (!userEntityService.checkAdmin() && !userEntityService.checkOwner(component)) {
            // Check permission (if access is granted, the policy that grants access is returned)
            policy = userEntityService.getFirstPolicyGrantingAccess(component, ACAccessType.READ_VALUE_LOGS, accessRequest)
                    .orElseThrow(() -> new MissingPermissionException("Component", component.getId(), ACAccessType.READ_VALUE_LOGS));
        }

        //Retrieve all value logs from database
        List<ValueLog> valueLogsList = valueLogRepository.findAllByIdRef(component.getId());

        //Filter value logs for start time if provided
        if ((startTime != null) && (startTime > 0)) {
            valueLogsList = valueLogsList.stream().filter(valueLog -> valueLog.getTime().toEpochMilli() >= startTime).collect(Collectors.toList());
        }

        //Filter value logs for end time if provided
        if ((endTime != null) && (endTime > 0)) {
            valueLogsList = valueLogsList.stream().filter(valueLog -> valueLog.getTime().toEpochMilli() < endTime).collect(Collectors.toList());
        }

        // Convert value logs to target unit if required
        if (S.notEmpty(unit)) {
            // Parse unit
            Unit<? extends Quantity> targetUnit;
            try {
                targetUnit = Unit.valueOf(unit);
            } catch (Exception e) {
                throw new MBPException(HttpStatus.BAD_REQUEST, "Invalid unit!");
            }

            // Get source unit
            Unit<? extends Quantity> sourceUnit = component.getOperator().getUnitObject();

            // Convert value logs using corresponding converter
            UnitConverter converter = sourceUnit.getConverterTo(targetUnit);
            for (ValueLog valueLog : valueLogsList) {
                unitConverterService.convertValueLogValue(valueLog, converter);
            }
        }
        // Apply effect (constraints)
        if (policy != null && policy.getEffectId() != null) {
            ACAbstractEffect effect = effectService.getForId(policy.getEffectId());
            valueLogsList.forEach(effect::apply);
        }

        // Iterate over all specified sort parameters
        for (Sort.Order order : pageable.getSort()) {
            // Only sorting for time property is supported, thus ignore the other ones
            if (!order.getProperty().equals("time")) {
                continue;
            }

            // Check sort direction and adjust document if necessary
            if (order.isDescending()) {
                Collections.reverse(valueLogsList);
            }

            // Only ordering for time is supported, so no need to consider other properties
            break;
        }

        //Create Page from results
        final int start = (int) pageable.getOffset();
        final int end = Math.min((start + pageable.getPageSize()), valueLogsList.size());
        return new PageImpl<>(valueLogsList.subList(start, end), pageable, valueLogsList.size());
    }
}
