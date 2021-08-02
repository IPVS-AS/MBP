package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesContainer;
import de.ipvs.as.mbp.domain.discovery.collections.revision.CandidateDevicesRevision;
import de.ipvs.as.mbp.domain.discovery.collections.revision.operations.RevisionOperation;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessage;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;

import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.INFO;
import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.SUCCESS;

/**
 * This task is responsible for revising the {@link CandidateDevicesCollection} that was received from a discovery
 * repository and stored for a certain {@link DeviceTemplate}, by integrating changes that are described as
 * {@link RevisionOperation}s within a {@link CandidateDevicesRevision}. Typically, such
 * {@link CandidateDevicesRevision}s are received as part of an asynchronous notification by the discovery repository
 * where a subscription for the corresponding {@link DeviceTemplate} has previously been created.
 */
public class ReviseCandidateDevicesTask implements CandidateDevicesTask {
    //The ID of the device template whose candidate devices are affected
    private String deviceTemplateId;

    //The name of the discovery repository that issued the notification
    private String repositoryName;

    //The revision describing the changes to the candidate devices
    private CandidateDevicesRevision revision;

    //The discovery log to extend for further log messages
    private DiscoveryLog discoveryLog;

    /*
    Injected fields
     */
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link ReviseCandidateDevicesTask} from a given ID of the {@link DeviceTemplate} whose
     * candidate devices are affected, the name of the repository that issued the notification, the
     * {@link CandidateDevicesRevision} describing the changes that were done to the candidate devices
     * and a {@link DiscoveryLog}.
     *
     * @param deviceTemplateId The ID of the {@link DeviceTemplate} whose candidate devices are affected
     * @param repositoryName   The name of the repository that issued the notification
     * @param revision         The {@link CandidateDevicesRevision} describing the changes to the candidate devices
     * @param discoveryLog     The {@link DiscoveryLog} to use for logging within this task
     */
    public ReviseCandidateDevicesTask(String deviceTemplateId, String repositoryName, CandidateDevicesRevision revision,
                                      DiscoveryLog discoveryLog) {
        //Set fields
        setDeviceTemplateId(deviceTemplateId);
        setRepositoryName(repositoryName);
        setRevision(revision);
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
        CandidateDevicesContainer candidateDevices = this.candidateDevicesRepository.findById(this.deviceTemplateId).orElse(null);

        //Check whether candidate devices could be found
        if (candidateDevices == null) {
            return;
        }

        //Write logs
        addLogMessage(String.format("Started task for device template %s.", deviceTemplateId));
        addLogMessage(String.format("Candidate devices consist out of %d unique " +
                        (candidateDevices.getCandidateDevicesCount() == 1 ? "device" : "devices") + " from %d " +
                        (candidateDevices.getCollectionsCount() == 1 ? "repository." : "repositories."),
                candidateDevices.getCandidateDevicesCount(), candidateDevices.getCollectionsCount()));
        addLogMessage(String.format("Received revision from repository \"%s\":\n%s", this.repositoryName,
                this.revision.toHumanReadableDescription()));

        //Check whether the candidate devices contain already a collection for this discovery repository
        if (candidateDevices.contains(this.repositoryName)) {
            //Apply operations of the revision to the existing candidate devices collection
            this.revision.applyOperations(candidateDevices.get(this.repositoryName));
        } else {
            //Create a new candidate devices collection
            CandidateDevicesCollection collection = new CandidateDevicesCollection(this.repositoryName);

            //Apply the operations
            this.revision.applyOperations(collection);

            //Add the collection to the candidate devices
            candidateDevices.addCandidateDevices(collection);
        }

        //Write log
        addLogMessage(String.format("Candidate devices consist out of %d unique " +
                        (candidateDevices.getCandidateDevicesCount() == 1 ? "device" : "devices") + " from %d " +
                        (candidateDevices.getCollectionsCount() == 1 ? "repository." : "repositories."),
                candidateDevices.getCandidateDevicesCount(), candidateDevices.getCollectionsCount()));

        //Save the updated candidate devices object to the repository
        candidateDevicesRepository.save(candidateDevices);

        //Write log
        addLogMessage(SUCCESS, "Saved updated candidate devices.");
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
     * Sets the ID of the {@link DeviceTemplate} whose candidate devices are affected.
     *
     * @param deviceTemplateId The ID of the {@link DeviceTemplate} to set
     * @return The task
     */
    public ReviseCandidateDevicesTask setDeviceTemplateId(String deviceTemplateId) {
        //Sanity check
        if ((deviceTemplateId == null) || deviceTemplateId.isEmpty())
            throw new IllegalArgumentException("The device template ID must not be null or empty.");

        this.deviceTemplateId = deviceTemplateId;
        return this;
    }

    /**
     * Returns the name of the discovery repository that issued the notification and transmitted the
     * corresponding {@link CandidateDevicesRevision}.
     *
     * @return The name of the discovery repository
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the name of the discovery repository that issued the notification and transmitted the
     * corresponding {@link CandidateDevicesRevision}.
     *
     * @param repositoryName The name of the discovery repository to set
     * @return The task
     */
    private ReviseCandidateDevicesTask setRepositoryName(String repositoryName) {
        //Sanity check
        if ((repositoryName == null) || repositoryName.isEmpty())
            throw new IllegalArgumentException("The repository name must not be null or empty.");

        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Returns the {@link CandidateDevicesRevision} describing the changes to the candidate devices.
     *
     * @return The {@link CandidateDevicesRevision}
     */
    public CandidateDevicesRevision getRevision() {
        return revision;
    }

    /**
     * Sets the {@link CandidateDevicesRevision} describing the changes to the candidate devices.
     *
     * @param revision The {@link CandidateDevicesRevision} to set
     * @return The task
     */
    public ReviseCandidateDevicesTask setRevision(CandidateDevicesRevision revision) {
        //Null check
        if (revision == null) throw new IllegalArgumentException("The revision must not be null.");

        this.revision = revision;
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
        return this.deviceTemplateId;
    }

    /**
     * Returns a simple, short and human-readable description of the task.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableString() {
        return "[Revise candidate devices]";
    }
}
