package de.ipvs.as.mbp.service.messaging.scatter_gather.config;

import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;

/**
 * Base class for request stage configurations that serve as descriptions of scatter gather request stages and can
 * be used in the {@link ScatterGatherRequestBuilder} to add such stages to a scatter gather request under construction.
 * A request stage configuration consists out of a request topic under which the request is supposed to be published,
 * a return topic that describes the topic under which the replies are expected to be published, a request
 * message of an arbitrary type that is supposed to be published under the request topic, a timeout value that indicates
 * until which point in time replies to a request are accepted and an expected number of replies which allows to close
 * the receiving phase before the timeout occurs.
 *
 * @param <T> The type of the request message
 */
public abstract class RequestStageConfig<T> {
    //Topic under which the request is supposed to be published
    private String requestTopic;

    //Request message to publish
    private T requestMessage;

    //Timeout after which the request should conclude at latest
    private int timeout = 60 * 1000; //milliseconds

    //Expected number of replies to close the receiving phase before the timout
    private int expectedReplies = Integer.MAX_VALUE;

    /**
     * Creates a new request stage configuration from a given request topic and request message. For the timeout,
     * the default value of one minute is used, while the number of expected replies is set to
     * {@link Integer.MAX_VALUE}.
     *
     * @param requestTopic   The request topic to use
     * @param requestMessage The request message to use
     */
    public RequestStageConfig(String requestTopic, T requestMessage) {
        //Set fields
        setRequestTopic(requestTopic);
        setRequestMessage(requestMessage);
    }

    /**
     * Returns the return topic which describes the topic under which the replies to the request of this stage
     * are expected to be published.
     *
     * @return The return topic
     */
    public abstract String getReturnTopic();

    /**
     * Returns the request topic of the scatter gather configuration.
     *
     * @return The request topic
     */
    public String getRequestTopic() {
        return requestTopic;
    }

    /**
     * Sets the request topic of the scatter gather configuration.
     *
     * @param requestTopic The request topic to set
     * @return The configuration
     */
    public RequestStageConfig<T> setRequestTopic(String requestTopic) {
        //Sanity check
        if ((requestTopic == null) || requestTopic.isEmpty()) {
            throw new IllegalArgumentException("Request topic must not be null or empty.");
        }

        //Set request topic
        this.requestTopic = requestTopic;
        return this;
    }

    /**
     * Returns the request message of the scatter gather configuration.
     *
     * @return The request message
     */
    public T getRequestMessage() {
        return requestMessage;
    }

    /**
     * Sets the request message of the scatter gather configuration.
     *
     * @param requestMessage The request message to set
     * @return The configuration
     */
    public RequestStageConfig<T> setRequestMessage(T requestMessage) {
        //Sanity check
        if ((requestMessage == null) || requestMessage.toString().isEmpty()) {
            throw new IllegalArgumentException("Request message must not be null or empty.");
        }

        this.requestMessage = requestMessage;
        return this;
    }

    /**
     * Returns the timeout value (in milliseconds) of the scatter gather configuration.
     *
     * @return The timeout value
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout value (in milliseconds) of the scatter gather configuration. It must be between 10 and 60000
     * milliseconds, while the default is one minute.
     *
     * @param timeout The timeout value to set
     * @return The configuration
     */
    public RequestStageConfig<T> setTimeout(int timeout) {
        //Sanity check
        if ((timeout < 10) || (timeout > 60000)) {
            throw new IllegalArgumentException("The timeout value must be between 10 and 60000 milliseconds.");
        }

        //Set timeout value
        this.timeout = timeout;
        return this;
    }

    /**
     * Returns the number of expected replies of the scatter gather configuration.
     *
     * @return The number of expected replies
     */
    public int getExpectedReplies() {
        return expectedReplies;
    }

    /**
     * Sets the number of expected replies of the scatter gather configuration. It must be greater than one, while the
     * default value is {@link Integer.MAX_VALUE}.
     *
     * @param expectedReplies The number of expected replies to set
     * @return The configuration
     */
    public RequestStageConfig<T> setExpectedReplies(int expectedReplies) {
        //Sanity check
        if (expectedReplies < 1) {
            throw new IllegalArgumentException("The number of expected replies must not be null.");
        }

        //Set number of expected replies
        this.expectedReplies = expectedReplies;
        return this;
    }
}
