package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResultContainer;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.service.discovery.gateway.CandidateDevicesSubscriber;
import de.ipvs.as.mbp.service.discovery.gateway.DiscoveryGateway;

import java.util.Collection;
import java.util.Objects;

/**
 * Updates the candidate devices that are stored as {@link CandidateDevicesResultContainer} in the
 * {@link CandidateDevicesRepository} for a certain {@link DeviceTemplate} by requesting the most recent
 * candidate devices from the discovery repositories. Optionally, a subscription can be created at the repositories.
 */
public class UpdateCandidateDevicesTask implements DeviceTemplateTask {

    //The device template to update the candidate devices for
    private DeviceTemplate deviceTemplate;

    //The request topics to use for retrieving the candidate devices
    private Collection<RequestTopic> requestTopics;

    //Whether the candidate devices should always be updated or only if not available
    private boolean force = false;

    //The subscriber to use for subscriptions
    private CandidateDevicesSubscriber subscriber = null;

    /*
    Injected fields
     */
    private final CandidateDevicesRepository candidateDevicesRepository;
    private final DiscoveryGateway discoveryGateway;

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate} and a collection
     * of {@link RequestTopic}s.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        this(deviceTemplate, requestTopics, null, false);
    }

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate}, a collection
     * of {@link RequestTopic}s and a force flag.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param force          True, if the update of candidate device is forced; false if it is only done when no
     *                       candidate device information is available for the device template
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics, boolean force) {
        this(deviceTemplate, requestTopics, null, force);
    }

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate},a collection
     * of {@link RequestTopic}s and a {@link CandidateDevicesSubscriber}.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param subscriber     The subscriber to use or null if no subscription is supposed to be created
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics, CandidateDevicesSubscriber subscriber) {
        this(deviceTemplate, requestTopics, subscriber, false);
    }

    /**
     * Creates a new {@link UpdateCandidateDevicesTask} from a given {@link DeviceTemplate}, a collection
     * of {@link RequestTopic}s, a {@link CandidateDevicesSubscriber} and a force flag.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics to use for retrieving the candidate devices
     * @param subscriber     The subscriber to use or null if no subscription is supposed to be created
     * @param force          True, if the update of candidate device is forced; false if it is only done when no
     *                       candidate device information is available for the device template
     */
    public UpdateCandidateDevicesTask(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics, CandidateDevicesSubscriber subscriber, boolean force) {
        //Set fields
        setDeviceTemplate(deviceTemplate);
        setRequestTopics(requestTopics);
        setSubscriber(subscriber);
        setForce(force);

        //Inject components
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
        this.discoveryGateway = DynamicBeanProvider.get(DiscoveryGateway.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Check if candidate devices are already available and not forced
        if ((!this.force) && this.candidateDevicesRepository.existsById(deviceTemplate.getId())) {
            return;
        }

        //Not available or forced, thus retrieve the candidate devices
        CandidateDevicesResultContainer candidateDevices = this.discoveryGateway.getDeviceCandidatesWithSubscription(this.deviceTemplate, this.requestTopics, this.subscriber);

        //Save the candidate devices to repository
        candidateDevicesRepository.save(candidateDevices);
    }

    /**
     * Returns the ID of the device template on which this tasks operates.
     *
     * @return The ID of the device template
     */
    @Override
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
}
