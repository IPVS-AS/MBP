package org.citopt.connde.web.rest;

import org.citopt.connde.RestConfiguration;
import org.citopt.connde.domain.component.Component;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.ComponentRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.deploy.ComponentState;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for requests related to the deployment state of components.
 *
 * @author Jan
 */
@RestController
@RequestMapping(RestConfiguration.BASE_PATH)
public class RestComponentStateController {
    @Autowired
    private SSHDeployer sshDeployer;

    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    /**
     * Responds with the deployment state for all actuators in the actuator repository as a map.
     *
     * @return A map (actuator id -> actuator state) that contains the state of each actuator
     */
    @GetMapping("/actuators/state")
    public ResponseEntity<Map<String, ComponentState>> getStatesAllActuators() {
        return getStatesAllComponents(actuatorRepository);
    }

    /**
     * Responds with the deployment state for all sensors in the sensor repository as a map.
     *
     * @return A map (sensor id -> sensor state) that contains the state of each sensor
     */
    @GetMapping("/sensors/state")
    public ResponseEntity<Map<String, ComponentState>> getStatesAllSensors() {
        return getStatesAllComponents(sensorRepository);
    }

    /**
     * Responds with the availability state for a certain actuator.
     *
     * @param actuatorId The id of the actuator whose state is supposed to be retrieved
     * @return The deployment state of the actuator as plain string
     */
    @GetMapping("/actuators/state/{id}")
    public ResponseEntity<ComponentState> getActuatorState(@PathVariable(value = "id") String actuatorId) {
        return getComponentState(actuatorId, actuatorRepository);
    }

    /**
     * Responds with the availability state for a certain sensor.
     *
     * @param sensorId The id of the sensor whose state is supposed to be retrieved
     * @return The deployment state of the sensor as plain string
     */
    @GetMapping("/sensors/state/{id}")
    public ResponseEntity<ComponentState> getSensorState(@PathVariable(value = "id") String sensorId) {
        return getComponentState(sensorId, sensorRepository);
    }

    private ResponseEntity<Map<String, ComponentState>> getStatesAllComponents(ComponentRepository repository) {
        //Create result map (component id -> component state)
        Map<String, ComponentState> resultMap = new HashMap<>();

        //Get all components
        List<Component> componentList = repository.findAll();

        //Iterate over all components and determine their state
        for (Component component : componentList) {
            ComponentState state = sshDeployer.determineComponentState(component);
            resultMap.put(component.getId(), state);
        }

        return new ResponseEntity<>(resultMap, HttpStatus.OK);
    }

    private ResponseEntity<ComponentState> getComponentState(String componentId, ComponentRepository repository) {
        //Retrieve component from repository
        Component component = (Component) repository.findOne(componentId);

        //Validity check
        if (component == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        //Determine component state
        ComponentState componentState = sshDeployer.determineComponentState(component);

        return new ResponseEntity<>(componentState, HttpStatus.OK);
    }
}
