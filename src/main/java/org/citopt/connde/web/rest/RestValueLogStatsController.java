package org.citopt.connde.web.rest;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.stats.ValueLogStatsService;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
@Api(tags = {"Value logs statistics"}, description = "Retrieval of statistics for recorded value logs")
public class RestValueLogStatsController {

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private ValueLogStatsService valueLogStatsService;

    @Autowired
    private UserEntityService userEntityService;

    @Autowired
    private MonitoringHelper monitoringHelper;

    /**
     * Responds with the value log stats for a certain actuator.
     *
     * @param actuatorId The id of the actuator whose value log stats are supposed to be retrieved
     * @param unit       A string specifying the desired unit of the value log stats
     * @return The value log stats of the actuator
     */
    @GetMapping("/actuators/{id}/stats")
    @ApiOperation(value = "Retrieves a list of statistics for recorded actuator value logs in a certain unit", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access statistics for value logs of this actuator"), @ApiResponse(code = 404, message = "Actuator not found or not authorized to access the actuator")})
    public ResponseEntity<ValueLogStats> getActuatorValueLogStats(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
                                                                  @RequestParam(value = "unit", required = false) String unit) {
        //Get actuator
        Actuator actuator = (Actuator) userEntityService.getUserEntityFromRepository(actuatorRepository, actuatorId);

        //Validity check
        if (actuator == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if user is permitted
        if (!userEntityService.isUserPermitted(actuator, "deploy")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Retrieve value log statistics
        return calculateValueLogStats(actuator, unit);
    }

    /**
     * Responds with the value log stats for a certain sensor.
     *
     * @param sensorId The id of the sensor whose value log stats are supposed to be retrieved
     * @param unit     A string specifying the desired unit of the value log stats
     * @return The value log stats of the sensor
     */
    @GetMapping("/sensors/{id}/stats")
    @ApiOperation(value = "Retrieves a list of statistics for recorded sensor value logs in a certain unit", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access statistics for value logs of this sensor"), @ApiResponse(code = 404, message = "Sensor not found or not authorized to access the sensor")})
    public ResponseEntity<ValueLogStats> getSensorValueLogStats(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
                                                                @RequestParam(value = "unit", required = false) String unit) {
        //Get sensor object
        Sensor sensor = (Sensor) userEntityService.getUserEntityFromRepository(sensorRepository, sensorId);

        //Validity check
        if (sensor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if user is permitted
        if (!userEntityService.isUserPermitted(sensor, "deploy")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Retrieve value log statistics
        return calculateValueLogStats(sensor, unit);
    }

    /**
     * Responds with the value log stats for a certain monitoring component.
     *
     * @param deviceId            The id of the device for which the stats are supposed to be retrieved
     * @param monitoringAdapterId The id of the monitoring adapter for which the stats are supposed to be retrieved
     * @param unit                A string specifying the desired unit of the value log stats
     * @return The value log stats of the sensor
     */
    @GetMapping("/monitoring/{deviceId}/stats")
    @ApiOperation(value = "Retrieves a list of statistics for recorded monitoring value logs in a certain unit", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access statistics for value logs of this monitoring"), @ApiResponse(code = 404, message = "Device or monitoring adapter not found or not authorized to access them")})
    public ResponseEntity<ValueLogStats> getMonitoringValueLogStats(@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
                                                                    @RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter to retrieve value log statistics for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
                                                                    @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value log statistics", example = "°C", required = false) String unit) {
        //Create new monitoring component from parameters
        MonitoringComponent monitoringComponent = monitoringHelper.createMonitoringComponent(deviceId, monitoringAdapterId);

        //Validity check
        if (monitoringComponent == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if user is permitted
        if (!userEntityService.isUserPermitted(monitoringComponent.getDevice(), "monitor")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Retrieve value log statistics
        return calculateValueLogStats(monitoringComponent, unit);
    }

    /**
     * Calculates the stats from value logs of a certain component in order to satisfy a server request.
     *
     * @param component The component for which the value log stats should be retrieved
     * @param unit      A string specifying the unit to which the values are supposed to be converted
     * @return The server response containing an object that holds the calculated data
     */
    private ResponseEntity<ValueLogStats> calculateValueLogStats(Component component, String unit) {
        //Convert given unit to object (if possible)
        Unit<? extends Quantity> convertUnit = null;
        if ((unit != null) && (!unit.isEmpty())) {
            //Try to parse unit
            try {
                convertUnit = Unit.valueOf(unit);
            } catch (Exception e) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }

        //Calculate stats by using the corresponding service
        ValueLogStats stats = valueLogStatsService.calculateValueLogStats(component, convertUnit);

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}
