package de.ipvs.as.mbp.domain.device;

import de.ipvs.as.mbp.domain.monitoring.MonitoringComponent;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import de.ipvs.as.mbp.service.event_handler.ICreateEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creation event handler for device entities.
 */
@Service
public class DeviceCreateEventHandler implements ICreateEventHandler<Device> {

    @Autowired
    private MonitoringOperatorRepository monitoringOperatorRepository;

    @Autowired
    private CEPTriggerService triggerService;

    /**
     * Called in case an entity has been created and saved successfully.
     *
     * @param entity The created entity
     */
    @Override
    public void onCreate(Device entity) {
        // Get all monitoring adapters
        List<MonitoringOperator> monitoringAdapters = monitoringOperatorRepository.findAll();

        // Iterate over all monitoring adapters and register an event type for the
        // resulting monitoring component
        for (MonitoringOperator monitoringAdapter : monitoringAdapters) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(monitoringAdapter, entity);
            triggerService.registerComponentEventType(monitoringComponent);
        }
    }
}
