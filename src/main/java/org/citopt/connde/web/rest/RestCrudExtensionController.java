package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller that extends the CRUD operations on basic resources.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestCrudExtensionController {
    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @RequestMapping(value = "/adapters/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAdapterHook(@PathVariable(value = "id") String adapterId) {
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByAdapterId(adapterId);
        for(ComponentProjection actuator : affectedActuators){
            actuatorRepository.delete(actuator.getId());
        }

        List<ComponentProjection> affectedSensors = sensorRepository.findAllByAdapterId(adapterId);
        for(ComponentProjection sensor : affectedSensors){
            sensorRepository.delete(sensor.getId());
        }

        adapterRepository.delete(adapterId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/devices/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteDeviceHook(@PathVariable(value = "id") String deviceId) {
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);
        for(ComponentProjection actuator : affectedActuators){
            actuatorRepository.delete(actuator.getId());
        }

        List<ComponentProjection> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);
        for(ComponentProjection sensor : affectedSensors){
            sensorRepository.delete(sensor.getId());
        }

        deviceRepository.delete(deviceId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/components/by-adapter/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<ComponentProjection>> getSensorsByAdapterID(@PathVariable(value = "id") String adapterId) {
        List<ComponentProjection> componentList = new ArrayList<>();
        componentList.addAll(actuatorRepository.findAllByAdapterId(adapterId));
        componentList.addAll(sensorRepository.findAllByAdapterId(adapterId));

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }

    @RequestMapping(value = "/components/by-device/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<ComponentProjection>> getSensorsByDeviceID(@PathVariable(value = "id") String deviceId) {
        List<ComponentProjection> componentList = new ArrayList<>();

        componentList.addAll(actuatorRepository.findAllByDeviceId(deviceId));
        componentList.addAll(sensorRepository.findAllByDeviceId(deviceId));

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }
}
