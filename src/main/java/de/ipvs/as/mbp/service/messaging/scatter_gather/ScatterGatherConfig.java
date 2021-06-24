package de.ipvs.as.mbp.service.messaging.scatter_gather;

/**
 * Objects of this class represent scatter gather configurations that can be used in the {@link ScatterGatherRequestBuilder}.
 * A configuration consists out of a request topic under which the request is supposed to be published,
 * a timeout value that indicates until which point in time replies to a request are accepted and an expected
 * number of replies which allows to close the receiving phase before the timeout occurs.
 */
public class ScatterGatherConfig {
    private String requestTopic;
    private int timeout = 60 * 1000; //milliseconds
    private int expectedReplies = Integer.MAX_VALUE;

    /**
     * Creates a new scatter gather configuration for a given request topic. For the timeout, the default value
     * of one minute is used, while the number of expected replies is set to {@link Integer.MAX_VALUE}.
     *
     * @param requestTopic The request topic to use
     */
    public ScatterGatherConfig(String requestTopic) {
        //Set request topic
        setRequestTopic(requestTopic);
    }

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
     * @return The request
     */
    public ScatterGatherConfig setRequestTopic(String requestTopic) {
        //Sanity check
        if ((requestTopic == null) || requestTopic.isEmpty()) {
            throw new IllegalArgumentException("Request topic must not be null or empty.");
        }

        //Set request topic
        this.requestTopic = requestTopic;
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
     * @return The request
     */
    public ScatterGatherConfig setTimeout(int timeout) {
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
     * @return The request
     */
    public ScatterGatherConfig setExpectedReplies(int expectedReplies) {
        //Sanity check
        if (expectedReplies < 1) {
            throw new IllegalArgumentException("The number of expected replies must not be null.");
        }

        //Set number of expected replies
        this.expectedReplies = expectedReplies;
        return this;
    }
}
