package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;

/**
 * This task is responsible for checking whether the {@link CandidateDevicesResult} that is stored for a
 * certain {@link DeviceTemplate} is currently used by any {@link DynamicPeripheral}s because they are deployed to a
 * device. If this is not the case, this task takes care of deleting the {@link CandidateDevicesResult} and
 * cancelling the subscriptions for asynchronous notifications at the individual discovery repositories.
 */
public class DeleteCandidateDevicesTask implements DeviceTemplateTask {

    //The device template to update the candidate devices for
    private DeviceTemplate deviceTemplate;

    /*
    Injected fields
     */
    private final DiscoveryGateway discoveryGateway;
    private final DynamicPeripheralRepository dynamicPeripheralRepository;
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link DeleteCandidateDevicesTask} from a given {@link DeviceTemplate}.
     *
     * @param deviceTemplate The device template to use
     */
    public DeleteCandidateDevicesTask(DeviceTemplate deviceTemplate) {
        //Set fields
        setDeviceTemplate(deviceTemplate);

        //Inject components
        this.discoveryGateway = DynamicBeanProvider.get(DiscoveryGateway.class);
        this.dynamicPeripheralRepository = DynamicBeanProvider.get(DynamicPeripheralRepository.class);
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Stream through all dynamic peripherals that use the provided device template
        boolean isDeviceTemplateInUse = this.dynamicPeripheralRepository
                .findByDeviceTemplateId(this.deviceTemplate.getId()).stream() //Find peripherals by device template ID
                .anyMatch(DynamicPeripheral::isActiveIntended); //Check whether any of them is intended to be active

        //Abort if candidate devices of the device template are in use
        if (isDeviceTemplateInUse) return;

        //Candidate devices are not in use, so delete them
        this.candidateDevicesRepository.deleteById(getDeviceTemplateId());

        //Send message to discovery repositories in order to cancel the subscriptions
        this.discoveryGateway.cancelSubscription(deviceTemplate);
    }

    /**
     * Returns the device template of this task.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return this.deviceTemplate;
    }

    /**
     * Sets the device template of this task.
     *
     * @param deviceTemplate The device template to set
     * @return The task
     */
    public DeleteCandidateDevicesTask setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        this.deviceTemplate = deviceTemplate;
        return this;
    }

    /**
     * Returns the ID of the {@link DeviceTemplate} on which this task operates.
     *
     * @return The ID of the device template
     */
    @Override
    public String getDeviceTemplateId() {
        return this.deviceTemplate.getId();
    }
}
