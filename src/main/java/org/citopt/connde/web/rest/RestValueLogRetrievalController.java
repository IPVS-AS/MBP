package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.*;
import org.citopt.connde.service.UnitConverterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.measure.converter.UnitConverter;
import javax.measure.unit.Unit;
import java.util.Iterator;

/**
 * REST Controller for retrieving value logs for certain components. Furthermore, it provides
 * features for converting the value log values to desired units.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestValueLogRetrievalController {

    @Autowired
    UnitConverterService unitConverterService;

    @Autowired
    ValueLogRepository valueLogRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    /**
     * Replies with a pageable list of value logs of a certain actuator.
     *
     * @param actuatorId       The id of the actuator for which the value logs should be retrieved
     * @param unit     A string specifying the unit of the value log values
     * @param pageable Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/actuators/{id}/valueLogs")
    public ResponseEntity<Page<ValueLog>> getActuatorValueLogs(@PathVariable(value = "id") String actuatorId,
                                                               @RequestParam(value = "unit", required = false) String unit,
                                                               Pageable pageable) {
        //Get actuator object
        Actuator actuator = (Actuator) getComponentById(actuatorId, actuatorRepository);
        return getValueLogs(actuator, unit, pageable);
    }

    /**
     * Replies with a pageable list of value logs of a certain sensor.
     *
     * @param sensorId       The id of the sensor for which the value logs should be retrieved
     * @param unit     A string specifying the unit of the value log values
     * @param pageable Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/sensors/{id}/valueLogs")
    public ResponseEntity<Page<ValueLog>> getSensorValueLogs(@PathVariable(value = "id") String sensorId,
                                                             @RequestParam(value = "unit", required = false) String unit,
                                                             Pageable pageable) {
        //Get sensor object
        Sensor sensor = (Sensor) getComponentById(sensorId, sensorRepository);
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
    public ResponseEntity<Page<ValueLog>> getMonitoringValueLogs(@PathVariable(value = "deviceId") String deviceId,
                                                                 @RequestParam("adapter") String monitoringAdapterId,
                                                                 @RequestParam(value = "unit", required = false) String unit,
                                                                 Pageable pageable) {
        //Get monitoring component object
        MonitoringComponent monitoringComponent = getMonitoringComponent(deviceId, monitoringAdapterId);
        return getValueLogs(monitoringComponent, unit, pageable);
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
        //Component not found?
        if (component == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        //Get value logs for this component
        Page<ValueLog> page = valueLogRepository.findAllByIdref(component.getId(), pageable);

        //Check if a valid unit was provided, otherwise return the result already
        if ((unit == null) || unit.isEmpty()) {
            return new ResponseEntity<>(page, HttpStatus.OK);
        }

        //Try to get unit object from string
        Unit targetUnit = null;
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
     * Fetches a component with a certain id from its repository.
     *
     * @param componentId The id of the component that is supposed to be retrieved
     * @param repository  The repository in which the component is contained
     * @return The corresponding component object
     */
    private Component getComponentById(String componentId, ComponentRepository repository) {
        return (Component) repository.findOne(componentId);
    }


    /**
     * Retrieves a monitoring component object for a device and a monitoring adapter, given by their ids.
     *
     * @param deviceId            The id of the device that is supposed to be part of the component
     * @param monitoringAdapterId The id of the monitoring adapter that is supposed to be part of the component
     * @return The resulting monitoring component
     */
    private MonitoringComponent getMonitoringComponent(String deviceId, String monitoringAdapterId) {
        //Find device and monitoring adapter in their repositories
        Device device = deviceRepository.findOne(deviceId);
        MonitoringAdapter monitoringAdapter = monitoringAdapterRepository.findOne(monitoringAdapterId);

        //Ensure that both objects could be found
        if ((device == null) || (monitoringAdapter == null)) {
            return null;
        }

        return new MonitoringComponent(monitoringAdapter, device);
    }
}
