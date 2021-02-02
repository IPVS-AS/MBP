package de.ipvs.as.mbp.domain.monitoring;

import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.repository.DeviceRepository;
import de.ipvs.as.mbp.repository.MonitoringOperatorRepository;
import de.ipvs.as.mbp.service.cep.trigger.CEPTriggerService;
import de.ipvs.as.mbp.service.event_handler.ICreateEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Creation event handler for monitoring operator entities.
 */
@Service
public class MonitoringOperatorCreateEventHandler implements ICreateEventHandler<MonitoringOperator> {

    @Autowired
    private DeviceRepository deviceRepository;

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
    public void onCreate(MonitoringOperator entity) {
        //Get all devices
        List<Device> devices = deviceRepository.findAll();

        //Iterate over all devices and register an event type for the resulting monitoring component
        for (Device device : devices) {
            MonitoringComponent monitoringComponent = new MonitoringComponent(entity, device);
            triggerService.registerComponentEventType(monitoringComponent);
        }
    }
}
