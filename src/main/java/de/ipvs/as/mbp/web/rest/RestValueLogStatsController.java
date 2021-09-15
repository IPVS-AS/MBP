package de.ipvs.as.mbp.web.rest;

import de.ipvs.as.mbp.constants.Constants;
import de.ipvs.as.mbp.domain.access_control.ACAbstractEffect;
import de.ipvs.as.mbp.domain.access_control.ACAccessRequest;
import de.ipvs.as.mbp.domain.access_control.ACAccessType;
import de.ipvs.as.mbp.domain.access_control.ACPolicy;
import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MBPException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.repository.ActuatorRepository;
import de.ipvs.as.mbp.repository.SensorRepository;
import de.ipvs.as.mbp.service.user.UserEntityService;
import de.ipvs.as.mbp.service.access_control.ACEffectService;
import de.ipvs.as.mbp.service.stats.ValueLogStatsService;
import de.ipvs.as.mbp.service.stats.model.ValueLogStats;
import de.ipvs.as.mbp.util.S;
import de.ipvs.as.mbp.web.rest.helper.MonitoringHelper;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

/**
 * REST Controller for requests related to the value log stats of components.
 */
@RestController
@RequestMapping(Constants.BASE_PATH)
@Api(tags = {"Value logs statistics"}, description = "Retrieval of statistics for recorded value logs")
public class RestValueLogStatsController {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ACEffectService effectService;

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
     * @throws MissingPermissionException
     */
    @GetMapping("/actuators/{id}/stats")
    @ApiOperation(value = "Retrieves a list of statistics for recorded actuator value logs in a certain unit", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value log statistics of this actuator!"),
            @ApiResponse(code = 404, message = "Actuator or requesting user not found!")})
    public ResponseEntity<ValueLogStats> getActuatorValueLogStats(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
            @RequestParam(value = "unit", required = false) String unit) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve actuator from the database (includes access-control)
        Actuator actuator = userEntityService.getForIdWithAccessControlCheck(actuatorRepository, actuatorId, ACAccessType.READ_VALUE_LOG_STATS, ACAccessRequest.valueOf(accessRequestHeader));

        // Calculate value log stats
        return ResponseEntity.ok(calculateValueLogStats(actuator, unit, ACAccessRequest.valueOf(accessRequestHeader)));
    }

    /**
     * Responds with the value log stats for a certain sensor.
     *
     * @param sensorId The id of the sensor whose value log stats are supposed to be
     *                 retrieved
     * @param unit     A string specifying the desired unit of the value log stats
     * @return The value log stats of the sensor
     * @throws EntityNotFoundException
     * @throws MissingPermissionException
     */
    @GetMapping("/sensors/{id}/stats")
    @ApiOperation(value = "Retrieves a list of statistics for recorded sensor value logs in a certain unit", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value log statistics of this sensor!"),
            @ApiResponse(code = 404, message = "Sensor or requesting user not found!")})
    public ResponseEntity<ValueLogStats> getSensorValueLogStats(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
            @RequestParam(value = "unit", required = false) String unit) throws EntityNotFoundException, MissingPermissionException {
        // Retrieve actuator from the database (includes access-control)
        Sensor sensor = userEntityService.getForIdWithAccessControlCheck(sensorRepository, sensorId, ACAccessType.READ_VALUE_LOG_STATS, ACAccessRequest.valueOf(accessRequestHeader));

        // Calculate value log stats
        return ResponseEntity.ok(calculateValueLogStats(sensor, unit, ACAccessRequest.valueOf(accessRequestHeader)));
    }

    /**
     * Responds with the value log stats for a certain monitoring component.
     *
     * @param deviceId             The id of the device for which the stats are
     *                             supposed to be retrieved
     * @param monitoringOperatorId The id of the monitoring operator for which the
     *                             stats are supposed to be retrieved
     * @param unit                 A string specifying the desired unit of the value
     *                             log stats
     * @return The value log stats of the sensor
     * @throws MissingPermissionException In case of missing permissions
     * @throws EntityNotFoundException    In case an entity could not be found
     */
    @GetMapping("/monitoring/{deviceId}/stats")
    @ApiOperation(value = "Retrieves a list of statistics for recorded monitoring value logs in a certain unit", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success!"),
            @ApiResponse(code = 400, message = "Invalid unit specification!"),
            @ApiResponse(code = 401, message = "Not authorized to access value log statistics of this monitoring component!"),
            @ApiResponse(code = 404, message = "Device, monitoring operator or requesting user not found!")})
    public ResponseEntity<ValueLogStats> getMonitoringValueLogStats(
            @RequestHeader("X-MBP-Access-Request") String accessRequestHeader,
            @PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
            @RequestParam("monitoringOperatorId") @ApiParam(value = "ID of the monitoring operator to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringOperatorId,
            @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value log statistics", example = "°C", required = false) String unit) throws MissingPermissionException, EntityNotFoundException {
        // Create new monitoring component from parameters
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringOperatorId);

        // Check monitoring permission
        userEntityService.requirePermission(monitoringComponent.getDevice(), ACAccessType.MONITOR, ACAccessRequest.valueOf(accessRequestHeader));

        // Retrieve value log statistics
        return ResponseEntity.ok(calculateValueLogStats(monitoringComponent, unit, ACAccessRequest.valueOf(accessRequestHeader)));
    }

    /**
     * Calculates the value log statistics for a give component and unit.
     *
     * @param <C>        the component type.
     * @param component  the {@link Component}.
     * @param unitString the unit specification as {@code String}.
     * @return the {@link ValueLogStats}.
     * @throws MissingPermissionException
     * @throws EntityNotFoundException
     */
    private <C extends Component> ValueLogStats calculateValueLogStats(C component, String unitString, ACAccessRequest accessRequest) throws MissingPermissionException, EntityNotFoundException {
        ACAbstractEffect effect = null;
        if (!userEntityService.checkAdmin() && !userEntityService.checkOwner(component)) {
            // Check permission (if access is granted, the policy that grants access is returned)
            ACPolicy policy = userEntityService.getFirstPolicyGrantingAccess(component, ACAccessType.READ_VALUE_LOG_STATS, accessRequest)
                    .orElseThrow(() -> new MissingPermissionException("Component", component.getId(), ACAccessType.READ_VALUE_LOGS));
            effect = effectService.getForId(policy.getEffectId());
        }

        // Parse unit
        Unit<? extends Quantity> unit = null;
        if (S.notEmpty(unitString)) {
            try {
                unit = Unit.valueOf(unitString);
            } catch (Exception e) {
                throw new MBPException(HttpStatus.BAD_REQUEST, "Invalid unit!");
            }
        }

        // Calculate stats
        return valueLogStatsService.calculateValueLogStats(component, unit, effect);
    }
}
