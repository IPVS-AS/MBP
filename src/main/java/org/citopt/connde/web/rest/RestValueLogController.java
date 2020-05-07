package org.citopt.connde.web.rest;

import io.swagger.annotations.*;
import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.*;
import org.citopt.connde.service.UnitConverterService;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;

/**
 * REST Controller for retrieving value logs for certain components. Furthermore, it provides
 * features for converting the value log values to desired units.
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
@Api(tags = {"Value logs"}, description = "Retrieval of recorded value logs")
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
    private MonitoringHelper monitoringHelper;


    /**
     * Replies with a pageable list of value logs of a certain actuator.
     *
     * @param actuatorId The id of the actuator for which the value logs should be retrieved
     * @param unit       A string specifying the unit of the value log values
     * @param pageable   Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/actuators/{id}/valueLogs")
    @ApiOperation(value = "Retrieves a list of recorded actuator value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access value logs of this actuator"), @ApiResponse(code = 404, message = "Actuator not found or not authorized to access the actuator")})
    public ResponseEntity<Page<ValueLog>> getActuatorValueLogs(@PathVariable(value = "id") @ApiParam(value = "ID of the actuator to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String actuatorId,
                                                               @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the actuator values", example = "°C", required = false) String unit,
                                                               @ApiParam(value = "The page configuration", required = true) Pageable pageable) {
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

        //Retrieve value logs
        return getValueLogs(actuator, unit, pageable);
    }

    /**
     * Replies with a pageable list of value logs of a certain sensor.
     *
     * @param sensorId The id of the sensor for which the value logs should be retrieved
     * @param unit     A string specifying the unit of the value log values
     * @param pageable Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/sensors/{id}/valueLogs")
    @ApiOperation(value = "Retrieves a list of recorded sensor value log in a certain unit which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access value logs of this sensor"), @ApiResponse(code = 404, message = "Sensor not found or not authorized to access the sensor")})
    public ResponseEntity<Page<ValueLog>> getSensorValueLogs(@PathVariable(value = "id") @ApiParam(value = "ID of the sensor to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String sensorId,
                                                             @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the sensor values", example = "°C", required = false) String unit,
                                                             @ApiParam(value = "The page configuration", required = true) Pageable pageable) {
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

        //Retrieve value logs
        return getValueLogs(sensor, unit, pageable);
    }

    /**
     * Replies with a pageable list of value logs of a certain monitoring component.
     *
     * @param deviceId            The id of the device for which monitoring data is supposed to be retrieved
     * @param monitoringAdapterId The id of the monitoring adapter for which monitoring data is supposed to be retrieved
     * @param unit                A string specifying the unit of the value log values
     * @param pageable            Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/monitoring/{deviceId}/valueLogs")
    @ApiOperation(value = "Retrieves a list of recorded monitoring value logs in a certain unit which fit onto a given page", produces = "application/hal+json")
    @ApiResponses({@ApiResponse(code = 200, message = "Success"), @ApiResponse(code = 403, message = "Not authorized to access value logs of this monitoring"), @ApiResponse(code = 404, message = "Device or monitoring adapter not found or not authorized to access them")})
    public ResponseEntity<Page<ValueLog>> getMonitoringValueLogs(@PathVariable(value = "deviceId") @ApiParam(value = "ID of the device to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String deviceId,
                                                                 @RequestParam("adapter") @ApiParam(value = "ID of the monitoring adapter to retrieve value logs for", example = "5c97dc2583aeb6078c5ab672", required = true) String monitoringAdapterId,
                                                                 @RequestParam(value = "unit", required = false) @ApiParam(value = "The desired unit of the monitoring value logs", example = "°C", required = false) String unit,
                                                                 @ApiParam(value = "The page configuration", required = true) Pageable pageable) {
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

        //Retrieve value logs
        return getValueLogs(monitoringComponent, unit, pageable);
    }

    /**
     * Deletes all recorded +value logs of a certain actuator.
     *
     * @param actuatorId The id of the actuator whose data is supposed to be deleted
     * @return A response entity that may be returned to the client
     */
    @DeleteMapping("/actuators/{id}/valueLogs")
    @ApiIgnore("Currently not working")
    public ResponseEntity deleteActuatorValueLogs(@PathVariable(value = "id") String actuatorId) {
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

        //Delete value logs of this actuator
        return deleteValueLogs(actuator);
    }

    /**
     * Deletes all recorded +value logs of a certain sensor.
     *
     * @param sensorId The id of the sensor whose data is supposed to be deleted
     * @return A response entity that may be returned to the client
     */
    @DeleteMapping("/sensors/{id}/valueLogs")
    @ApiIgnore("Currently not working")
    public ResponseEntity deleteSensorValueLogs(@PathVariable(value = "id") String sensorId) {
        //Get sensor
        Sensor sensor = (Sensor) userEntityService.getUserEntityFromRepository(sensorRepository, sensorId);

        //Validity check
        if (sensor == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Check if user is permitted
        if (!userEntityService.isUserPermitted(sensor, "deploy")) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        //Delete value logs of this sensor
        return deleteValueLogs(sensor);
    }

    /**
     * Deletes all recorded +value logs of a certain monitoring component.
     *
     * @param deviceId            The id of the device whose data is supposed to be deleted
     * @param monitoringAdapterId The id of the device whose data is supposed to be deleted
     * @return A response entity that may be returned to the client
     */
    @DeleteMapping("/monitoring/{deviceId}/valueLogs")
    @ApiIgnore("Currently not working")
    public ResponseEntity deleteMonitoringValueLogs(@PathVariable(value = "deviceId") String deviceId,
                                                    @RequestParam("adapter") String monitoringAdapterId) {

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

        //Delete value logs of this component
        return deleteValueLogs(monitoringComponent);
    }

    /**
     * Returns a response entity that contains a pageable list of value logs of a certain component.
     *
     * @param component The component for which the value logs should be retrieved
     * @param unit      A string specifying the unit of the value log values
     * @param pageable  Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    private ResponseEntity<Page<ValueLog>> getValueLogs(Component component, String unit, Pageable pageable) {
        //Get value logs for this component
        Page<ValueLog> page = valueLogRepository.findAllByIdRef(component.getId(), pageable);

        //Check if a valid unit was provided, otherwise return the result already
        if ((unit == null) || unit.isEmpty()) {
            return new ResponseEntity<>(page, HttpStatus.OK);
        }

        //Try to get unit object from string
        Unit targetUnit;
        try {
            targetUnit = Unit.valueOf(unit);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Get unit object from adapter
        Unit startUnit = component.getAdapter().getUnitObject();

        //Get corresponding unit converter
        UnitConverter converter = startUnit.getConverterTo(targetUnit);

        //Iterate over all value logs of this
        for (ValueLog valueLog : page) {
            //Convert value
            unitConverterService.convertValueLogValue(valueLog, converter);
        }

        //All values converted, now return
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    /**
     * Deletes all recorded +value logs of a certain component.
     *
     * @param component The component whose data is supposed to be deleted
     * @return A response entity that may be returned to the client
     */
    private ResponseEntity deleteValueLogs(Component component) {
        valueLogRepository.deleteByIdRef(component.getId());

        //Return success response
        return new ResponseEntity(HttpStatus.OK);
    }
}
