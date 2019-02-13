package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
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
 * REST Controller that extends the CRUD operations on basic resources, e.g. for cascade deletion.
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

    /**
     * Called, when an adapter is supposed to be deleted. This method then takes care of deleting
     * the components which use this adapter.
     *
     * @param adapterId The id of the adapter that is supposed to be deleted
     * @return Empty response
     */
    @RequestMapping(value = "/adapters/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteAdapterHook(@PathVariable(value = "id") String adapterId) {
        //Find actuators that use the adapter and delete them all
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByAdapterId(adapterId);
        for (ComponentProjection actuator : affectedActuators) {
            actuatorRepository.delete(actuator.getId());
        }

        //Find sensors that use the adapter and delete them all
        List<ComponentProjection> affectedSensors = sensorRepository.findAllByAdapterId(adapterId);
        for (ComponentProjection sensor : affectedSensors) {
            sensorRepository.delete(sensor.getId());
        }

        //Finally delete the adapter
        adapterRepository.delete(adapterId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Called, when a device is supposed to be deleted. This method then takes care of deleting
     * the components which use this device.
     *
     * @param deviceId The id of the device that is supposed to be deleted
     * @return Empty response
     */
    @RequestMapping(value = "/devices/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteDeviceHook(@PathVariable(value = "id") String deviceId) {
        //Find actuators that use the device and delete them all
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection actuator : affectedActuators) {
            actuatorRepository.delete(actuator.getId());
        }

        //Find sensors that use the device and delete them all
        List<ComponentProjection> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection sensor : affectedSensors) {
            sensorRepository.delete(sensor.getId());
        }

        //Finally delete the device
        deviceRepository.delete(deviceId);

        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Returns a list of components that make use of a certain adapter.
     *
     * @param adapterId The id of the adapter for which using components should be found
     * @return A list of all components that make use of the adapter
     */
    @RequestMapping(value = "/components/by-adapter/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<ComponentProjection>> getComponentsByAdapterID(@PathVariable(value = "id") String adapterId) {
        //Create empty component list
        List<ComponentProjection> componentList = new ArrayList<>();

        //Add using actuators and sensors
        componentList.addAll(actuatorRepository.findAllByAdapterId(adapterId));
        componentList.addAll(sensorRepository.findAllByAdapterId(adapterId));

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }

    /**
     * Returns a list of components that make use of a certain device.
     *
     * @param deviceId The id of the device for which using components should be found
     * @return A list of all components that make use of the device
     */
    @RequestMapping(value = "/components/by-device/{id}", method = RequestMethod.GET)
    public ResponseEntity<List<ComponentProjection>> getComponentsByDeviceID(@PathVariable(value = "id") String deviceId) {
        //Create empty component list
        List<ComponentProjection> componentList = new ArrayList<>();

        //Add using actuators and sensors
        componentList.addAll(actuatorRepository.findAllByDeviceId(deviceId));
        componentList.addAll(sensorRepository.findAllByDeviceId(deviceId));

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }
}
