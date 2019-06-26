package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.deploy.SSHDeployer;
import org.citopt.connde.web.rest.helper.MonitoringHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

/**
 * Event handler for operations that are performed on monitoring adapters.
 */
@Component
@RepositoryEventHandler
public class MonitoringAdapterEventHandler {

    @Autowired
    private ValueLogRepository valueLogRepository;

    @Autowired
    private MonitoringHelper monitoringHelper;

    @Autowired
    private SSHDeployer sshDeployer;

    /**
     * Called in case a monitoring adapter is supposed to be deleted. This method then takes care of undeploying
     * the corresponding monitoring components (if necessary) and deleting the associated value logs.
     *
     * @param adapter The adapter that is supposed to be deleted
     */
    @HandleBeforeDelete
    public void beforeMonitoringAdapterDelete(MonitoringAdapter adapter) throws IOException {
        //Get all devices that are compatible to the monitoring adapter
        List<Device> compatibleDevices = monitoringHelper.getCompatibleDevices(adapter);

        //Iterate over all compatible devices
        for (Device device : compatibleDevices) {
            //Create monitoring component from monitoring adapter and device
            MonitoringComponent monitoringComponent = new MonitoringComponent(adapter, device);

            //Undeploy monitoring component if necessary
            sshDeployer.undeployIfRunning(monitoringComponent);

            //TODO Delete value logs with idref monitoringComponent.getId()
        }
    }
}
