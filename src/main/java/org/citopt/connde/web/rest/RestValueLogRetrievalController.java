package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
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

    /**
     * Replies with a pageable list of value logs of a certain actuator.
     *
     * @param id       The id of the actuator for which the value logs should be retrieved
     * @param unit     A string specifying the unit of the value log values
     * @param pageable Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/actuators/{id}/valueLogs")
    public ResponseEntity<Page<ValueLog>> getActuatorValueLogs(@PathVariable(value = "id") String id,
                                                               @RequestParam(value = "unit") String unit,
                                                               Pageable pageable) {
        return getValueLogs(id, actuatorRepository, unit, pageable);
    }

    /**
     * Replies with a pageable list of value logs of a certain sensor.
     *
     * @param id       The id of the sensor for which the value logs should be retrieved
     * @param unit     A string specifying the unit of the value log values
     * @param pageable Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    @GetMapping("/sensors/{id}/valueLogs")
    public ResponseEntity<Page<ValueLog>> getSensorValueLogs(@PathVariable(value = "id") String id,
                                                             @RequestParam(value = "unit") String unit,
                                                             Pageable pageable) {
        return getValueLogs(id, sensorRepository, unit, pageable);
    }

    /**
     * Returns a response entity that contains a pageable list of value logs of a certain sensor.
     *
     * @param id         The id of the component for which the value logs should be retrieved
     * @param repository A component repository that contains the component
     * @param unit       A string specifying the unit of the value log values
     * @param pageable   Pageable parameters that specify the value logs to retrieve
     * @return A pageable list of value logs
     */
    private ResponseEntity<Page<ValueLog>> getValueLogs(String id, ComponentRepository repository, String unit, Pageable pageable) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(id);

        //Component not found?
        if (component == null) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        //Get value logs for this component
        Page<ValueLog> page = valueLogRepository.findAllByIdref(id, pageable);

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
}
