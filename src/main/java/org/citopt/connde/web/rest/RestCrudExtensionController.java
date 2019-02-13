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
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestCrudExtensionController {
    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Returns a list of components that make use of a certain adapter.
     *
     * @param adapterId The id of the adapter for which using components should be found
     * @return A list of all components that make use of the adapter
     */
    @GetMapping("/components/by-adapter/{id}")
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
    @GetMapping("/components/by-device/{id}")
    public ResponseEntity<List<ComponentProjection>> getComponentsByDeviceID(@PathVariable(value = "id") String deviceId) {
        //Create empty component list
        List<ComponentProjection> componentList = new ArrayList<>();

        //Add using actuators and sensors
        componentList.addAll(actuatorRepository.findAllByDeviceId(deviceId));
        componentList.addAll(sensorRepository.findAllByDeviceId(deviceId));

        return new ResponseEntity<>(componentList, HttpStatus.OK);
    }
}
