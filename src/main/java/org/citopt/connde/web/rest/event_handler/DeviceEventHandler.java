package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on devices.
 *
 * @author Jan
 */
@Component
@RepositoryEventHandler(Device.class)
public class DeviceEventHandler {
    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called, when a device is supposed to be deleted. This method then takes care of deleting
     * the components which use this device.
     *
     * @param device The device that is supposed to be deleted
     * @return Empty response
     */
    @HandleBeforeDelete
    public void deleteDeviceHook(Device device) throws IOException {
        String deviceId = device.getId();

        //Find actuators that use the device and delete them after undeployed
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection projection : affectedActuators) {
            Actuator actuator = actuatorRepository.findOne(projection.getId());
            sshDeployer.undeployIfRunning(actuator);
            actuatorRepository.delete(projection.getId());
        }

        //Find sensors that use the device and delete them after undeployed
        List<ComponentProjection> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection projection : affectedSensors) {
            Sensor sensor = sensorRepository.findOne(projection.getId());
            sshDeployer.undeployIfRunning(sensor);
            sensorRepository.delete(projection.getId());
        }
    }
}