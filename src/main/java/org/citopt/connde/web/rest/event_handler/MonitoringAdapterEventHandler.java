package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringComponent;
import org.citopt.connde.repository.DeviceRepository;
import org.citopt.connde.service.cep.trigger.CEPTriggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Event handler for operations that are performed on monitoring adapters.
 */
@Component
@RepositoryEventHandler
public class MonitoringAdapterEventHandler {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private CEPTriggerService triggerService;

    /**
     * Called, when a monitoring adapter was created. This method then takes care of registering corresponding
     * event types for monitoring components at the CEP engine.
     *
     * @param monitoringAdapter The created monitoring adapter
     */
    public void afterMonitoringAdapterCreate(MonitoringAdapter monitoringAdapter) {
        //Get all devices
        List<Device> devices = deviceRepository.findAll();

        //Iterate over all devices and register an event type for the resulting monitoring component
        for (Device device : devices) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, device);
            triggerService.registerComponentEventType(monitoringComponent);
        }
    }
}