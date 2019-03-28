package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.stats.ValueLogStatsService;
import org.citopt.connde.service.stats.model.ValueLogStats;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ValueLogStatsService valueLogStatsService;

    /**
     * Responds with the value log stats for a certain actuator.
     *
     * @param actuatorId The id of the actuator whose value log stats are supposed to be retrieved
     * @return The value log stats of the actuator
     */
    @GetMapping("/actuators/stats/{id}")
    public ResponseEntity<ValueLogStats> getActuatorValueLogStats(@PathVariable(value = "id") String actuatorId,
                                                                  @RequestParam(value = "unit", required = false) String unit) {
        return calculateValueLogStats(actuatorId, actuatorRepository, unit);
    }

    /**
     * Responds with the value log stats for a certain sensor.
     *
     * @param sensorId The id of the sensor whose value log stats are supposed to be retrieved
     * @return The value log stats of the sensor
     */
    @GetMapping("/sensors/stats/{id}")
    public ResponseEntity<ValueLogStats> getSensorValueLogStats(@PathVariable(value = "id") String sensorId,
                                                                @RequestParam(value = "unit", required = false) String unit) {
        return calculateValueLogStats(sensorId, sensorRepository, unit);
    }

    /**
     * Calculates the stats from value logs of a certain component in order to satisfy a server request.
     *
     * @param id         The id of the component for which the stats are supposed to be computed
     * @param repository The component reposiotry in which the component can be found
     * @param unit       A string specifying the unit to which the values are supposed to be converted
     * @return The server response containing an object that holds the calculated data
     */
    private ResponseEntity<ValueLogStats> calculateValueLogStats(String id, ComponentRepository repository, String unit) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Component not found?
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
}
