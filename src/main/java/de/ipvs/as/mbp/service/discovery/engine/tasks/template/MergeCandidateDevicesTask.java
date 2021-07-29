package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessage;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;

import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.INFO;
import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.SUCCESS;

/**
 * This task is responsible for merging the {@link CandidateDevicesCollection} that was received from a discovery
 * repository as part of an asynchronous notification with the {@link CandidateDevicesResult} that is already
 * stored for the pertaining {@link DeviceTemplate}.
 */
public class MergeCandidateDevicesTask implements CandidateDevicesTask {

    //The device template whose candidate devices are affected
    private DeviceTemplate deviceTemplate;

    //The name of the discovery repository that issued the notification
    private String repositoryName;

    //The updated candidate devices as received from the discovery repository
    private CandidateDevicesCollection updatedCandidateDevices;

    //The discovery log to extend for further log messages
    private DiscoveryLog discoveryLog;

    /*
    Injected fields
     */
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link MergeCandidateDevicesTask} from a given {@link DeviceTemplate}, a repository name, a
     * {@link CandidateDevicesCollection} of updated candidate devices and a {@link DiscoveryLog}.
     *
     * @param deviceTemplate          The device template whose candidate devices are affected
     * @param repositoryName          The name of the repository that issued the notification
     * @param updatedCandidateDevices The updated candidate devices as received from the discovery repository
     * @param discoveryLog                The {@link DiscoveryLog} to use for logging within this task
     */
    public MergeCandidateDevicesTask(DeviceTemplate deviceTemplate, String repositoryName,
                                     CandidateDevicesCollection updatedCandidateDevices, DiscoveryLog discoveryLog) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setRepositoryName(repositoryName);
        setUpdatedCandidateDevices(updatedCandidateDevices);
        setDiscoveryLog(discoveryLog);

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

        //Write log
        addLogMessage(String.format("Started task for device template \"%s\".", deviceTemplate.getName()));
        addLogMessage(String.format("Merging %d known candidate devices of %d discovery repositories with the update of %d candidate devices.",
                candidateDevices.getCandidateDevicesCount(), candidateDevices.getCollectionsCount(),
                this.updatedCandidateDevices.size()));

        //Update the candidate devices or add them if none are available for the provided discovery repository name
        candidateDevices.replaceCandidateDevices(this.repositoryName, this.updatedCandidateDevices);

        //Write log
        addLogMessage(String.format("Saving merge result containing %d candidate devices from %d discovery repositories.",
                candidateDevices.getCandidateDevicesCount(), candidateDevices.getCollectionsCount()));

        //Save the updated candidate devices object to the repository again
        candidateDevicesRepository.save(candidateDevices);

        //Write log
        addLogMessage(SUCCESS, "Completed successfully.");
    }

    /**
     * Creates a new {@link DiscoveryLogMessage} from a given message string and adds it to the
     * {@link DiscoveryLog} that collects the logs of this task.
     *
     * @param message The actual log message
     */
    private void addLogMessage(String message) {
        //Delegate call
        addLogMessage(INFO, message);
    }

    /**
     * Creates a new {@link DiscoveryLogMessage} from a given message string and a {@link DiscoveryLogMessageType}
     * and adds it to the {@link DiscoveryLog} that collects the logs of this task.
     *
     * @param type    The type of the log message
     * @param message The actual log message
     */
    private void addLogMessage(DiscoveryLogMessageType type, String message) {
        //Check if log messages are supposed to be collected
        if (this.discoveryLog == null) return;

        //Update start timestamp when this is the first log message
        if (discoveryLog.isEmpty()) discoveryLog.updateStartTimestamp();

        //Create new log message
        DiscoveryLogMessage logMessage = new DiscoveryLogMessage(type, message);

        //Add the message to the discovery log of this task
        discoveryLog.addMessage(logMessage);
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
     * Returns the {@link DiscoveryLog} that is used within this task in order to collect
     * {@link DiscoveryLogMessage}s for logging purposes. May be null, if the task does not perform logging.
     *
     * @return The {@link DiscoveryLog} or null, if logging is not performed
     */
    @Override
    public DiscoveryLog getDiscoveryLog() {
        return discoveryLog;
    }

    /**
     * Sets the {@link DiscoveryLog} that is supposed to be used within this task in order to collect
     * {@link DiscoveryLogMessage}s for logging purposes. If set to null, logging is not formed.
     *
     * @param discoveryLog The {@link DiscoveryLog} or null, if no logging is supposed to be performed
     */
    private void setDiscoveryLog(DiscoveryLog discoveryLog) {
        this.discoveryLog = discoveryLog;
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
