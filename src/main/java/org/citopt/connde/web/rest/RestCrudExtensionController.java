package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller that extends the CRUD operations on basic resources, e.g. for cascade deletion.
 *
 * @author Jan
 */
@RestController
public class RestCrudExtensionController {
    @Autowired
    private AdapterRepository adapterRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called, when an actuator is supposed to be deleted. This method then takes care of undeploying and deleting it.
     *
     * @param actuatorId The id of the actuator that is supposed to be deleted
     * @return Empty response
     */
    @DeleteMapping(RestConfiguration.BASE_PATH + "/actuators/{id}")
    public ResponseEntity deleteActuatorHook(@PathVariable(value = "id") String actuatorId) {
        deleteActuatorSafely(actuatorId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Called, when a sensor is supposed to be deleted. This method then takes care of undeploying and deleting it.
     *
     * @param sensorId The id of the sensor that is supposed to be deleted
     * @return Empty response
     */
    @DeleteMapping(RestConfiguration.BASE_PATH + "/sensors/{id}")
    public ResponseEntity deleteSensorHook(@PathVariable(value = "id") String sensorId) {
        deleteActuatorSafely(sensorId);
        return new ResponseEntity(HttpStatus.NO_CONTENT);
    }

    /**
     * Called, when an adapter is supposed to be deleted. This method then takes care of deleting
     * the components which use this adapter.
     *
     * @param adapterId The id of the adapter that is supposed to be deleted
     * @return Empty response
     */
    @DeleteMapping(RestConfiguration.BASE_PATH + "/adapters/{id}")
    public ResponseEntity deleteAdapterHook(@PathVariable(value = "id") String adapterId) {
        //Find actuators that use the adapter and delete them all
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByAdapterId(adapterId);
        for (ComponentProjection actuator : affectedActuators) {
            deleteActuatorSafely(actuator.getId());
        }

        //Find sensors that use the adapter and delete them all
        List<ComponentProjection> affectedSensors = sensorRepository.findAllByAdapterId(adapterId);
        for (ComponentProjection sensor : affectedSensors) {
            deleteSensorSafely(sensor.getId());
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
    @DeleteMapping(RestConfiguration.BASE_PATH + "/devices/{id}")
    public ResponseEntity deleteDeviceHook(@PathVariable(value = "id") String deviceId) {
        //Find actuators that use the device and delete them all safely
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection actuator : affectedActuators) {
            deleteActuatorSafely(actuator.getId());
        }

        //Find sensors that use the device and delete them all safely
        List<ComponentProjection> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection sensor : affectedSensors) {
            deleteSensorSafely(sensor.getId());
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
    @GetMapping(RestConfiguration.BASE_PATH + "/components/by-adapter/{id}")
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
    @GetMapping(RestConfiguration.BASE_PATH + "/components/by-device/{id}")
    public ResponseEntity<List<ComponentProjection>> getComponentsByDeviceID(@PathVariable(value = "id") String deviceId) {
        //Create empty component list
        List<ComponentProjection> componentList = new ArrayList<>();

        //Add using actuators and sensors
        componentList.addAll(actuatorRepository.findAllByDeviceId(deviceId));
        componentList.addAll(sensorRepository.findAllByDeviceId(deviceId));

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }

    /**
     * Deletes an actuator in a safe manner by undeploying the actuator before deletion in case it
     * is has been deployed before.
     * @param actuatorId The id of the actuator to delete
     */
    private void deleteActuatorSafely(String actuatorId) {
        //Retrieve corresponding actuator object
        Actuator actuator = actuatorRepository.findOne(actuatorId);

        //Undeploy actuator if it is deployed
        try {
            if (sshDeployer.isComponentRunning(actuator)) {
                sshDeployer.undeployComponent(actuator);
            }
        } catch (IOException e) {
        }

        //Delete actuator finally
        actuatorRepository.delete(actuatorId);
    }

    /**
     * Deletes a sensor in a safe manner by undeploying the sensor before deletion in case it
     * is has been deployed before.
     * @param sensorId The id of the sensor to delete
     */
    private void deleteSensorSafely(String sensorId) {
        //Retrieve corresponding sensor object
        Sensor sensor = sensorRepository.findOne(sensorId);

        //Undeploy sensor if it is deployed
        try {
            if (sshDeployer.isComponentRunning(sensor)) {
                sshDeployer.undeployComponent(sensor);
            }
        } catch (IOException e) {
        }

        //Delete sensor finally
        sensorRepository.delete(sensorId);
    }
}
