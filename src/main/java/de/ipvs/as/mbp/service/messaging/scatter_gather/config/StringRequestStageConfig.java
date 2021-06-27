package de.ipvs.as.mbp.service.messaging.scatter_gather.config;

import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;

/**
 * Objects of this class represent request stage configurations that serve as descriptions of scatter gather request
 * stages for string request messages and can be used in the {@link ScatterGatherRequestBuilder} to add such stages to a
 * scatter gather request under construction. A request stage configuration consists out of a request topic under which
 * the request is supposed to be published, a return topic that describes the topic under which the replies are
 * expected to be published, a request message of an arbitrary type that is supposed to be published under the request
 * topic, a timeout value that indicates until which point in time replies to a request are accepted and an expected
 * number of replies which allows to close the receiving phase before the timeout occurs.
 */
public class StringRequestStageConfig extends RequestStageConfig<String> {
    //Topic for resulting reply messages
    private String returnTopic;

    /**
     * Creates a new request stage configuration for string request messages from a given request topic, return topic
     * and request message. For the timeout, the default value of one minute is used, while the number of
     * expected replies is set to {@link Integer.MAX_VALUE}.
     *
     * @param requestTopic      The request topic to use
     * @param returnTopic The return topic to use
     * @param requestMessage    The request message to use
     */
    public StringRequestStageConfig(String requestTopic, String returnTopic, String requestMessage) {
        //Call constructor of super class
        super(requestTopic, requestMessage);

        //Set return topic
        setReturnTopic(returnTopic);
    }


    /**
     * Returns the return topic which describes the topic under which the replies to the request of this stage
     * are expected to be published.
     *
     * @return The return topic
     */
    @Override
    public String getReturnTopic() {
        return this.returnTopic;
    }

    /**
     * Sets the return topic which describes the topic under which the replies to the request of this stage
     * are expected to be published.
     *
     * @param returnTopic The return topic to set
     * @return The request stage configuration
     */
    public StringRequestStageConfig setReturnTopic(String returnTopic) {
        //Sanity check
        if ((returnTopic == null) || returnTopic.isEmpty()) {
            throw new IllegalArgumentException("Return topic not be null or empty.");
        }

        //Set return topic
        this.returnTopic = returnTopic;
        return this;
    }
}
