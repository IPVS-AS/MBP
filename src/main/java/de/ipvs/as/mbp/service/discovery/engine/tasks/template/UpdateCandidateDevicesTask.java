package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLog;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessage;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;

import java.util.Collection;
import java.util.Objects;

import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.INFO;
import static de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessageType.SUCCESS;

/**
 * This task is responsible for updating the candidate devices that are stored as {@link CandidateDevicesResult} in the
 * {@link CandidateDevicesRepository} for a certain {@link DeviceTemplate} by requesting the most recent
 * candidate devices from the discovery repositories. Optionally, a subscription can be created at the repositories.
 */
public class UpdateCandidateDevicesTask implements CandidateDevicesTask {

    //The device template to update the candidate devices for
    private DeviceTemplate deviceTemplate;

    //The request topics to use for retrieving the candidate devices
    private Collection<RequestTopic> requestTopics;

    //Whether the candidate devices should always be updated or only if not available
    private boolean force = false;

    //The subscriber to use for subscriptions
    private CandidateDevicesSubscriber subscriber = null;

    //The log entry to extend for further log messages
    private DiscoveryLog logEntry;

    /*
    Injected fields
     */
    private final CandidateDevicesRepository candidateDevicesRepository;
    private final DynamicDeploymentRepository dynamicDeploymentRepository;
    private final DiscoveryGateway discoveryGateway;

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate}, a collection of
     * {@link RequestTopic}s and a {@link DiscoveryLog}.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param logEntry       The {@link DiscoveryLog} to use for logging within this task
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics,
                                      DiscoveryLog logEntry) {
        this(deviceTemplate, requestTopics, null, false, logEntry);
    }

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate}, a collection
     * of {@link RequestTopic}s, a force flag and a {@link DiscoveryLog}.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param force          True, if the update of candidate device is forced; false if it is only done when no
     *                       candidate device information is available for the device template
     * @param logEntry       The {@link DiscoveryLog} to use for logging within this task
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics,
                                      boolean force, DiscoveryLog logEntry) {
        this(deviceTemplate, requestTopics, null, force, logEntry);
    }

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate},a collection
     * of {@link RequestTopic}s, a {@link CandidateDevicesSubscriber} and a {@link DiscoveryLog}.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param subscriber     The subscriber to use or null if no subscription is supposed to be created
     * @param logEntry       The {@link DiscoveryLog} to use for logging within this task
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics,
                                      CandidateDevicesSubscriber subscriber, DiscoveryLog logEntry) {
        this(deviceTemplate, requestTopics, subscriber, false, logEntry);
    }

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate}, a collection
     * of {@link RequestTopic}s, a {@link CandidateDevicesSubscriber}, a force flag and a {@link DiscoveryLog}.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param subscriber     The subscriber to use or null if no subscription is supposed to be created
     * @param force          True, if the update of candidate device is forced; false if it is only done when no
     *                       candidate device information is available for the device template
     * @param logEntry       The {@link DiscoveryLog} to use for logging within this task
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics,
                                      CandidateDevicesSubscriber subscriber, boolean force, DiscoveryLog logEntry) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setRequestTopics(requestTopics);
        setSubscriber(subscriber);
        setForce(force);
        setLogEntry(logEntry);

        //Inject components
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
        this.dynamicDeploymentRepository = DynamicBeanProvider.get(DynamicDeploymentRepository.class);
        this.discoveryGateway = DynamicBeanProvider.get(DiscoveryGateway.class);
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

        //Abort if not forced and candidate devices of the device template are not in use
        if ((!this.force) && (!isDeviceTemplateInUse)) {
            return;
        }

        //Write log
        addLogMessage(String.format("Started task for device template \"%s\".", deviceTemplate.getName()));

        //Abort if not forced and candidate devices are already available
        if ((!this.force) && this.candidateDevicesRepository.existsById(deviceTemplate.getId())) {
            //Write log
            addLogMessage("Candidate devices are already available, thus aborting.");
            return;
        }

        //Write log
        addLogMessage("Requesting candidate devices from discovery repositories.");

        //Not available or forced, thus retrieve the candidate devices
        CandidateDevicesResult candidateDevices = this.discoveryGateway.getDeviceCandidatesWithSubscription(this.deviceTemplate, this.requestTopics, this.subscriber);

        //Write log
        addLogMessage(String.format("Received %s.", candidateDevices.toHumanReadableDescription()));

        //Save the candidate devices to repository
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
        if (this.logEntry == null) return;

        //Update start timestamp when this is the first log message
        if (logEntry.isEmpty()) logEntry.updateStartTimestamp();

        //Create new log message
        DiscoveryLogMessage logMessage = new DiscoveryLogMessage(type, message);

        //Add the message to the log entry of this task
        logEntry.addMessage(logMessage);
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
    public UpdateCandidateDevicesTask setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Null check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        this.deviceTemplate = deviceTemplate;
        return this;
    }

    /**
     * Returns the request topics under which the candidate devices requests are supposed to be published.
     *
     * @return The request topics
     */
    public Collection<RequestTopic> getRequestTopics() {
        return requestTopics;
    }

    /**
     * Sets the request topics under which the candidate devices requests are supposed to be published.
     *
     * @param requestTopics The request topics to set
     * @return The task
     */
    public UpdateCandidateDevicesTask setRequestTopics(Collection<RequestTopic> requestTopics) {
        //Null check
        if ((requestTopics == null) || requestTopics.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The request topics must not be null.");
        }

        this.requestTopics = requestTopics;
        return this;
    }

    /**
     * Returns whether the update of candidate devices is forced.
     *
     * @return True, if forced; false otherwise
     */
    public boolean isForce() {
        return force;
    }

    /**
     * Sets whether the update of candidate devices is forced.
     *
     * @param force True, if forced; false otherwise
     * @return The task
     */
    public UpdateCandidateDevicesTask setForce(boolean force) {
        this.force = force;
        return this;
    }

    /**
     * Returns the subscriber of this task or null if no subscription is supposed to be created.
     *
     * @return The subscriber or null
     */
    public CandidateDevicesSubscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Sets the subscriber of this task or null if no subscription is supposed to be created.
     *
     * @param subscriber The subscriber to set or null
     * @return The task
     */
    public UpdateCandidateDevicesTask setSubscriber(CandidateDevicesSubscriber subscriber) {
        this.subscriber = subscriber;
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
        return logEntry;
    }

    /**
     * Sets the {@link DiscoveryLog} that is supposed to be used within this task in order to collect
     * {@link DiscoveryLogMessage}s for logging purposes. If set to null, logging is not formed.
     *
     * @param logEntry The {@link DiscoveryLog} or null, if no logging is supposed to be performed
     */
    private void setLogEntry(DiscoveryLog logEntry) {
        this.logEntry = logEntry;
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
        return "[Update candidate devices]";
    }
}
