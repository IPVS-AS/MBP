package de.ipvs.as.mbp.service.messaging.scatter_gather.config;

import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.types.RequestMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;

/**
 * Objects of this class represent request stage configurations that serve as descriptions of scatter gather request
 * stages for domain {@link RequestMessage}s and can be used in the {@link ScatterGatherRequestBuilder} to add such
 * stages to a scatter gather request under construction. A request stage configuration consists out of a request
 * topic under which the request is supposed to be published, a return topic that describes the topic under
 * which the replies are expected to be published, a request message of an arbitrary type that is supposed to be
 * published under the request topic, a timeout value that indicates until which point in time replies to a request
 * are accepted and an expected number of replies which allows to close the receiving phase before the timeout occurs.
 * In case of {@link DomainRequestStageConfig}s, the return topic is derived from the request message itself.
 * If this message does not specify a return topic, a new and unique return topic will be generated within the
 * {@link ScatterGatherRequestBuilder}.
 *
 * @param <T> The type of the domain {@link RequestMessage}
 */
public class DomainRequestStageConfig<T extends RequestMessage<? extends DomainMessageBody>> extends RequestStageConfig<T> {
    /**
     * Creates a new request stage configuration for domain {@link RequestMessage}s from a given request topic and
     * request message. For the timeout, the default value of one minute is used, while the number of expected replies
     * is set to {@link Integer.MAX_VALUE}.
     *
     * @param requestTopic   The request topic to use
     * @param requestMessage The request message to use
     */
    public DomainRequestStageConfig(String requestTopic, T requestMessage) {
        //Call constructor of super class
        super(requestTopic, requestMessage);
    }


    /**
     * Returns the return topic which describes the topic under which the replies to the request of this stage
     * are expected to be published.
     *
     * @return The return topic
     */
    @Override
    public String getReturnTopic() {
        //Get return topic from request message
        return this.getRequestMessage().getReturnTopic();
    }
}
