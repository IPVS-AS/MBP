package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;

/**
 * This task is responsible for checking whether the {@link CandidateDevicesResult} that is stored for a
 * certain {@link DeviceTemplate} is currently used by any {@link DynamicDeployment}s because they are deployed to a
 * device. If this is not the case, this task takes care of deleting the {@link CandidateDevicesResult} and
 * cancelling the subscriptions for asynchronous notifications at the individual discovery repositories.
 */
public class DeleteCandidateDevicesTask implements DeviceTemplateTask {

    //The device template to update the candidate devices for
    private DeviceTemplate deviceTemplate;

    //Whether to force the deletion of candidate devices and the unsubscription
    private boolean force = false;

    /*
    Injected fields
     */
    private final DiscoveryGateway discoveryGateway;
    private final DynamicDeploymentRepository dynamicDeploymentRepository;
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link DeleteCandidateDevicesTask} from a given {@link DeviceTemplate}.
     *
     * @param deviceTemplate The device template to use
     */
    public DeleteCandidateDevicesTask(DeviceTemplate deviceTemplate) {
        this(deviceTemplate, false);
    }

    /**
     * Creates a new {@link DeleteCandidateDevicesTask} from a given {@link DeviceTemplate} and a force flag.
     *
     * @param deviceTemplate The device template to use
     * @param force          True, if the deletion of candidate devices and the unsubscription should be forced and thus
     *                       done without checking whether the corresponding device template is currently in use
     */
    public DeleteCandidateDevicesTask(DeviceTemplate deviceTemplate, boolean force) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setForce(force);

        //Inject components
        this.discoveryGateway = DynamicBeanProvider.get(DiscoveryGateway.class);
        this.dynamicDeploymentRepository = DynamicBeanProvider.get(DynamicDeploymentRepository.class);
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Stream through all dynamic deployments that use the provided device template
        boolean isDeviceTemplateInUse = this.dynamicDeploymentRepository
                .findByDeviceTemplate_Id(this.deviceTemplate.getId()).stream() //Find deployments by device template ID
                .anyMatch(DynamicDeployment::isActivatingIntended); //Check whether any of them is intended to be active

        //Abort if not forced and candidate devices of the device template are in use
        if ((!force) && isDeviceTemplateInUse) {
            System.out.println("Not deleted.");
            return;
        }

        //Candidate devices are not in use, so delete them
        this.candidateDevicesRepository.deleteById(getDeviceTemplateId());

        //Check whether a subscription exists
        if (!this.discoveryGateway.isSubscribed(deviceTemplate)) {
            System.out.println("No subscription.");
            return;
        }

        //Send message to discovery repositories in order to cancel the subscriptions
        this.discoveryGateway.cancelSubscription(deviceTemplate);
        System.out.println("************* Deleted and unsubscribed. *************");
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
     * Returns whether the deletion of candidate devices and the unsubscription is forced.
     *
     * @return True, if forced; false otherwise
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Sets whether the deletion of candidate devices and the unsubscription is forced.
     *
     * @param force True, if forced; false otherwise
     * @return The task
     */
    public DeleteCandidateDevicesTask setForce(boolean force) {
        this.force = force;
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

    /**
     * Returns a simple, short and human-readable description of the task.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableString() {
        return "[Delete candidate devices]";
    }
}
