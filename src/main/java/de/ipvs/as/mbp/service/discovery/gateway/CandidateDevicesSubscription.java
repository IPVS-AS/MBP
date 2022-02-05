package de.ipvs.as.mbp.service.discovery.gateway;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;

/**
 * Objects of this class model asynchronous subscriptions that are created at the discovery repositories
 * in order to become notified when the collection of suitable candidate devices, which could be determined
 * on behalf of a given {@link DeviceTemplate}, changed over time at a certain repository.
 */
class CandidateDevicesSubscription {

    //The device template for which the subscription is created
    private DeviceTemplate deviceTemplate;

    //Request topics that were used for creating the subscription
    private Collection<RequestTopic> requestTopics;

    //The subscriber to notify about changes in the collection candidate devices for a device template
    private CandidateDevicesSubscriber subscriber;


    //The point in time at which the subscription was created
    private Instant creationTimestamp = Instant.now();

    /**
     * Creates a new candidate devices subscription object from a given {@link DeviceTemplate}, a {@link Collection}
     * of {@link RequestTopic}s and a {@link CandidateDevicesSubscriber}.
     *
     * @param deviceTemplate The device template to use
     * @param requestTopics  The request topics that were originally used for creating the subscription
     * @param subscriber     The subscriber to use
     */
    protected CandidateDevicesSubscription(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics, CandidateDevicesSubscriber subscriber) {
        setDeviceTemplate(deviceTemplate);
        setRequestTopics(requestTopics);
        setSubscriber(subscriber);
    }

    /**
     * Returns the device template for which the subscription is created.
     *
     * @return The device template
     */
    public DeviceTemplate getDeviceTemplate() {
        return deviceTemplate;
    }

    /**
     * Sets the device template for which the subscription is created.
     *
     * @param deviceTemplate The device template to set
     * @return The candidate devices subscription
     */
    public CandidateDevicesSubscription setDeviceTemplate(DeviceTemplate deviceTemplate) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        this.deviceTemplate = deviceTemplate;
        return this;
    }

    /**
     * Returns the {@link RequestTopic}s that were originally used to create the subscription.
     *
     * @return The request topics
     */
    public Collection<RequestTopic> getRequestTopics() {
        return requestTopics;
    }

    /**
     * Sets the {@link RequestTopic}s that were originally used to create the subscription.
     *
     * @param requestTopics The request topics to set
     * @return The candidate devices subscription
     */
    public CandidateDevicesSubscription setRequestTopics(Collection<RequestTopic> requestTopics) {
        //Null check
        if ((requestTopics == null) || requestTopics.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The request topics must not be null or empty.");
        }

        this.requestTopics = requestTopics;
        return this;
    }

    /**
     * Returns the {@link CandidateDevicesSubscriber} of the subscription that is supposed to be notified about changes.
     *
     * @return The subscriber
     */
    public CandidateDevicesSubscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Sets the {@link CandidateDevicesSubscriber} of the subscription that is supposed to be notified about changes.
     *
     * @param subscriber The subscriber to set
     * @return The candidate devices subscription
     */
    public CandidateDevicesSubscription setSubscriber(CandidateDevicesSubscriber subscriber) {
        this.subscriber = subscriber;
        return this;
    }

    /**
     * Returns the creation timestamp.
     *
     * @return The creation timestamp
     */
    public Instant getCreationTimestamp() {
        return creationTimestamp;
    }

    /**
     * Updates the creation timestamp
     *
     * @return The candidate devices subscription
     */
    public CandidateDevicesSubscription updateCreationTimestamp() {
        this.creationTimestamp = Instant.now();
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CandidateDevicesSubscription)) return false;
        CandidateDevicesSubscription that = (CandidateDevicesSubscription) o;
        return Objects.equals(subscriber, that.subscriber) && Objects.equals(deviceTemplate, that.deviceTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subscriber, deviceTemplate);
    }
}