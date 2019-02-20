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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<ValueLogStats> getActuatorValueLogStats(@PathVariable(value = "id") String actuatorId) {
        return calculateValueLogStats(actuatorId, actuatorRepository);
    }

    /**
     * Responds with the value log stats for a certain sensor.
     *
     * @param sensorId The id of the sensor whose value log stats are supposed to be retrieved
     * @return The value log stats of the sensor
     */
    @GetMapping("/sensors/stats/{id}")
    public ResponseEntity<ValueLogStats> getSensorValueLogStats(@PathVariable(value = "id") String sensorId) {
        return calculateValueLogStats(sensorId, sensorRepository);
    }

    private ResponseEntity<ValueLogStats> calculateValueLogStats(String id, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Component not found?
        if (component == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        //Calculate stats by using the corresponding service
        ValueLogStats stats = valueLogStatsService.calculateValueLogStats(component);

        return new ResponseEntity<>(stats, HttpStatus.OK);
    }
}
