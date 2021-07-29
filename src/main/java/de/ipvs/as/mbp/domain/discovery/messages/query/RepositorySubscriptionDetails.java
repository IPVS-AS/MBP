package de.ipvs.as.mbp.domain.discovery.messages.query;

/**
 * Objects of this class model details of intended subscriptions on the discovery repositories which may lead to
 * asynchronous notifications in case a query result set on the repository changes due to the addition, removal or
 * modification of device descriptions.
 */
public class RepositorySubscriptionDetails {
    //ID of the affected device template, serving as identifier for asynchronous notifications
    private String referenceId;

    //Return topic under which notifications are supposed to be published
    private String returnTopic;

    /**
     * Creates a new, empty subscription details object.
     */
    public RepositorySubscriptionDetails() {

    }

    /**
     * Creates a new subscription details object from a given reference ID and return topic.
     *
     * @param referenceId The reference ID to use
     * @param returnTopic The return topic to use
     */
    public RepositorySubscriptionDetails(String referenceId, String returnTopic) {
        //Set fields
        setReferenceId(referenceId);
        setReturnTopic(returnTopic);
    }

    /**
     * Returns the reference ID which can be used to connect notifications with the pertained device template.
     *
     * @return The reference ID
     */
    public String getReferenceId() {
        return referenceId;
    }

    /**
     * Sets the reference ID which can be used to connect notifications with the pertained device template.
     *
     * @param referenceId The reference ID to set
     * @return The subscription details object
     */
    public RepositorySubscriptionDetails setReferenceId(String referenceId) {
        this.referenceId = referenceId;
        return this;
    }

    /**
     * Returns the return topic under which notifications are supposed to be published.
     *
     * @return The return topic
     */
    public String getReturnTopic() {
        return returnTopic;
    }

    /**
     * Sets the return topic under which notifications are supposed to be published.
     *
     * @param returnTopic The return topic to set
     * @return The subscription details object
     */
    public RepositorySubscriptionDetails setReturnTopic(String returnTopic) {
        this.returnTopic = returnTopic;
        return this;
    }
}
