package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;

/**
 * This task is responsible for merging the {@link CandidateDevicesCollection} that was received from a discovery
 * repository as part of an asynchronous notification with the {@link CandidateDevicesResult} that is already
 * stored for the pertaining {@link DeviceTemplate}.
 */
public class MergeCandidateDevicesTask implements DeviceTemplateTask {

    //The device template whose candidate devices are affected
    private DeviceTemplate deviceTemplate;

    //The name of the discovery repository that issued the notification
    private String repositoryName;

    //The updated candidate devices as received from the discovery repository
    private CandidateDevicesCollection updatedCandidateDevices;

    /*
    Injected fields
     */
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link MergeCandidateDevicesTask} from a given {@link DeviceTemplate}, a repository name
     * and a {@link CandidateDevicesCollection} of updated candidate devices.
     *
     * @param deviceTemplate          The device template whose candidate devices are affected
     * @param repositoryName          The name of the repository that issued the notification
     * @param updatedCandidateDevices The updated candidate devices as received from the discovery repository
     */
    public MergeCandidateDevicesTask(DeviceTemplate deviceTemplate, String repositoryName, CandidateDevicesCollection updatedCandidateDevices) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setRepositoryName(repositoryName);
        setUpdatedCandidateDevices(updatedCandidateDevices);

        //Inject components
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read all candidate devices for the device template from the repository
        CandidateDevicesResult candidateDevices = this.candidateDevicesRepository.findById(this.deviceTemplate.getId()).orElse(null);

        //Check whether candidate devices could be found
        if (candidateDevices == null) {
            return;
        }

        //Update the candidate devices or add them if none are available for the provided discovery repository name
        candidateDevices.replaceCandidateDevices(this.repositoryName, this.updatedCandidateDevices);

        //Save the updated candidate devices object to the repository again
        candidateDevicesRepository.save(candidateDevices);
    }

    /**
     * Returns the device template whose candidate devices are affected.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template whose candidate devices are affected.
     *
     * @param deviceTemplate The device template to set
     * @return The task
     */
    private MergeCandidateDevicesTask setDeviceTemplate(DeviceTemplate deviceTemplate) {
        this.deviceTemplate = deviceTemplate;
        return this;
    }

    /**
     * Returns the name of the discovery repository that issued the notification and transmitted the updated
     * candidate devices data.
     *
     * @return The repository name
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the name of the discovery repository that issued the notification and transmitted the updated candidate
     * devices data.
     *
     * @param repositoryName The repository name to set
     * @return The task
     */
    private MergeCandidateDevicesTask setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Returns the {@link CandidateDevicesCollection} of updated candidate devices as received from the discovery
     * repository.
     *
     * @return The updated candidate devices
     */
    public CandidateDevicesCollection getUpdatedCandidateDevices() {
        return updatedCandidateDevices;
    }

    /**
     * Sets the {@link CandidateDevicesCollection} of updated candidate devices as received from the discovery
     * repository.
     *
     * @param updatedCandidateDevices The updated candidate devices to set
     * @return The task
     */
    private MergeCandidateDevicesTask setUpdatedCandidateDevices(CandidateDevicesCollection updatedCandidateDevices) {
        this.updatedCandidateDevices = updatedCandidateDevices;
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
        return "[Merge candidate devices]";
    }
}
