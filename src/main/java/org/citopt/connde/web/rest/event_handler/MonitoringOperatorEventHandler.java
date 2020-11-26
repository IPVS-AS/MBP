package org.citopt.connde.web.rest.event_handler;

import java.io.IOException;
import java.util.List;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringOperator;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Event handler for operations that are performed on monitoring operators.
 */
@Component
@RepositoryEventHandler
public class MonitoringOperatorEventHandler {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private CEPTriggerService triggerService;

    @Autowired
    private MonitoringHelper monitoringHelper;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called in case a monitoring operator was created. This method then takes care of registering corresponding
     * event types for monitoring components at the CEP engine.
     *
     * @param monitoringOperator The created monitoring operator
     */
    @HandleAfterCreate
    public void afterMonitoringOperatorCreate(MonitoringOperator monitoringOperator) {
        //Get all devices
        List<Device> devices = deviceRepository.findAll();

        //Iterate over all devices and register an event type for the resulting monitoring component
        for (Device device : devices) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringOperator, device);
            triggerService.registerComponentEventType(monitoringComponent);
        }
    }

    /**
     * Called in case a monitoring operator is supposed to be deleted. This method then takes care of undeploying
     * the corresponding monitoring components (if necessary) and deleting the associated value logs.
     *
     * @param operator The operator that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeMonitoringOperatorDelete(MonitoringOperator operator) throws IOException {
        //Get all devices that are compatible to the monitoring operator
        List<Device> compatibleDevices = monitoringHelper.getCompatibleDevices(operator);

        //Iterate over all compatible devices
        for (Device device : compatibleDevices) {
            //Create monitoring component from monitoring operator and device
            MonitoringComponent monitoringComponent = new MonitoringComponent(operator, device);

            //Undeploy monitoring component if necessary
            sshDeployer.undeployIfRunning(monitoringComponent);

            //TODO Delete value logs by idref monitoringComponent.getId()
        }
    }
}