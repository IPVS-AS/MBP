package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.*;
import org.citopt.connde.service.stats.ValueLogStatsService;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.measure.unit.Unit;

/**
 * REST Controller for requests related to the value log stats of components.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestValueLogStatsController {
    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    @Autowired
    private ValueLogStatsService valueLogStatsService;

    /**
     * Responds with the value log stats for a certain actuator.
     *
     * @param actuatorId The id of the actuator whose value log stats are supposed to be retrieved
     * @param unit       A string specifying the desired unit of the value log stats
     * @return The value log stats of the actuator
     */
    @GetMapping("/actuators/{id}/stats")
    public ResponseEntity<ValueLogStats> getActuatorValueLogStats(@PathVariable(value = "id") String actuatorId,
                                                                  @RequestParam(value = "unit", required = false) String unit) {
        //Get actuator object
        Actuator actuator = (Actuator) getComponentById(actuatorId, actuatorRepository);
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
    public ResponseEntity<ValueLogStats> getSensorValueLogStats(@PathVariable(value = "id") String sensorId,
                                                                @RequestParam(value = "unit", required = false) String unit) {
        //Get sensor object
        Sensor sensor = (Sensor) getComponentById(sensorId, sensorRepository);
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
    public ResponseEntity<ValueLogStats> getMonitoringValueLogStats(@PathVariable(value = "deviceId") String deviceId,
                                                                    @RequestParam("adapter") String monitoringAdapterId,
                                                                    @RequestParam(value = "unit", required = false) String unit) {
        //Get monitoring component object
        MonitoringComponent monitoringComponent = getMonitoringComponent(deviceId, monitoringAdapterId);
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
        //Validity check
        if (component == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        //Convert given unit to object (if possible)
        Unit convertUnit = null;
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
