package de.ipvs.as.mbp.service.discovery.gateway;

import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionCollection;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.time.Instant;
import java.util.Objects;

/**
 * Objects of this class model asynchronous subscriptions that are created at the discovery repositories
 * in order to become notified when the collection of suitable candidate devices, which could be determined
 * on behalf of a given {@link DeviceTemplate}, changed over time at a certain repository.
 */
class CandidateDevicesSubscription {

    //The subscriber to notify about changes in the collection candidate devices for a device template
    private CandidateDevicesSubscriber subscriber;

    //The device template for which the subscription is created
    private DeviceTemplate deviceTemplate;

    //The point in time at which the subscription was created
    private Instant creationTimestamp = Instant.now();

    /**
     * Creates a new candidate devices subscription object from a given {@link DeviceTemplate} and
     * {@link CandidateDevicesSubscriber}.
     *
     * @param deviceTemplate The device template to use
     * @param subscriber The subscriber to use
     */
    protected CandidateDevicesSubscription(DeviceTemplate deviceTemplate, CandidateDevicesSubscriber subscriber) {
        setDeviceTemplate(deviceTemplate);
        setSubscriber(subscriber);
    }

    /**
     * Returns the {@link CandidateDevicesSubscriber} of the subscription that is supposed to be notified about changes.
     * @return The subscriber
     */
    public CandidateDevicesSubscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Sets the {@link CandidateDevicesSubscriber} of the subscription that is supposed to be notified about changes.
     * @param subscriber The subscriber to set
     * @return The candidate devices subscription
     */
    public CandidateDevicesSubscription setSubscriber(CandidateDevicesSubscriber subscriber) {
        this.subscriber = subscriber;
        return this;
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
