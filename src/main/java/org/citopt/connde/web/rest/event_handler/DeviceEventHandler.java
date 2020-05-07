package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.ActuatorRepository;
import org.citopt.connde.repository.MonitoringAdapterRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
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
    private ValueLogRepository valueLogRepository;

    @Autowired
    private MonitoringAdapterRepository monitoringAdapterRepository;

    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private MonitoringHelper monitoringHelper;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called in case a device was created. This method then takes care of registering corresponding
     * event types for monitoring components at the CEP engine.
     *
     * @param device The created device
     */
    @HandleAfterCreate
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
     * Called in case a device is supposed to be deleted. This method then takes care of deleting
     * the components which use this device.
     *
     * @param device The device that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeDeviceDelete(Device device) throws IOException {
        //Get device id
        String deviceId = device.getId();

        //Find actuators that use this device and iterate over them
        List<ComponentExcerpt> affectedActuators = actuatorRepository.findAllByDeviceId(deviceId);
        for (ComponentExcerpt projection : affectedActuators) {
            Actuator actuator = actuatorRepository.findOne(projection.getId());

            //Undeploy actuator if running
            sshDeployer.undeployIfRunning(actuator);

            //TODO Delete value logs with idref monitoringComponent.getId()

            //Delete actuator
            actuatorRepository.delete(projection.getId());
        }

        //Find sensors that use the device and delete them after undeployment
        List<ComponentExcerpt> affectedSensors = sensorRepository.findAllByDeviceId(deviceId);
        for (ComponentExcerpt projection : affectedSensors) {
            Sensor sensor = sensorRepository.findOne(projection.getId());

            //Undeploy sensor if running
            sshDeployer.undeployIfRunning(sensor);

            //TODO Delete value logs with idref sensor.getId()

            //Delete sensor
            sensorRepository.delete(projection.getId());
        }

        //Get all monitoring adapters that are compatible to the device
        List<MonitoringAdapter> compatibleMonitoringAdapters = monitoringHelper.getCompatibleAdapters(device);

        //Iterate over the compatible monitoring adapters
        for (MonitoringAdapter adapter : compatibleMonitoringAdapters) {
            //Create monitoring component from monitoring adapter and device
            MonitoringComponent monitoringComponent = new MonitoringComponent(adapter, device);

            //Undeploy monitoring component if necessary
            sshDeployer.undeployIfRunning(monitoringComponent);

            //TODO Delete value logs with idref monitoringComponent.getId()
        }
    }
}