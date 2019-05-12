package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.projection.ComponentProjection;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on devices.
 */
@Component
@RepositoryEventHandler
public class DeviceEventHandler {
    @Autowired
    private ActuatorRepository actuatorRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called, when a device was created. This method then takes care of registering corresponding
     * event types for monitoring components at the CEP engine.
     *
     * @param device The created device
     */
    public void afterDeviceCreate(Device device) {
        //Get all monitoring adapters
        List<MonitoringAdapter> monitoringAdapters = monitoringAdapterRepository.findAll();

        //Iterate over all monitoring adapters and register an event type for the resulting monitoring component
        for (MonitoringAdapter monitoringAdapter : monitoringAdapters) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);
            triggerService.registerComponentEventType(monitoringComponent);
        }
    }

    /**
     * Called, when a device is supposed to be deleted. This method then takes care of deleting
     * the components which use this device.
     *
     * @param device The device that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeDeviceDelete(Device device) throws IOException {
        String deviceId = device.getId();

        //Find actuators that use the device and delete them after undeployed
        List<ComponentProjection> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection projection : affectedActuators) {
            Actuator actuator = actuatorRepository.findOne(projection.getId());
            sshDeployer.undeployIfRunning(actuator);
            actuatorRepository.delete(projection.getId());
        }

        //Find sensors that use the device and delete them after undeployment
        List<ComponentProjection> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);
        for (ComponentProjection projection : affectedSensors) {
            Sensor sensor = sensorRepository.findOne(projection.getId());
            sshDeployer.undeployIfRunning(sensor);
            sensorRepository.delete(projection.getId());
        }
    }
}