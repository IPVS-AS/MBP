package de.ipvs.as.mbp.service.discovery;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestReply;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestRequest;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.types.ReplyMessage;
import de.ipvs.as.mbp.service.messaging.message.types.RequestMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequest;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;
import de.ipvs.as.mbp.service.messaging.scatter_gather.config.DomainRequestStageConfig;
import de.ipvs.as.mbp.service.messaging.scatter_gather.config.RequestStageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * This service offers various discovery-related functions that require communication with the discovery repositories
 * of the user's IoT environment. However, the interface of this service operates only on discovery-specific methods
 * and domain objects and thus hides the messaging-related logic and implementations that run in the background.
 * This way, messaging-related tasks and idiosyncrasies can be hidden from the components that make use of this gateway.
 * For example, since all methods that are offered by this gateway operate in a synchronous manner from the perspective
 * of a using component, the asynchronous nature of communication via messaging can be entirely encapsulated.
 */
@Service
public class DiscoveryGateway {

    //Suffixes of the topics to use for publishing requests //TODO move to annotation?
    private static final String TOPIC_SUFFIX_TEST_REQUEST = "/test";

    //The publish-subscribe-based messaging service to use
    @Autowired
    private PubSubService pubSubService;

    /**
     * Creates and initializes the discovery gateway.
     */
    public DiscoveryGateway() {

    }

    public Map<String, Integer> getAvailableRepositories(RequestTopic requestTopic) {
        //Sanity check
        if (requestTopic == null) {
            throw new IllegalArgumentException("Request topic must not be null.");
        }

        //Create result map
        Map<String, Integer> repositoryMap = new HashMap<>();

        //Create request message body
        DiscoveryTestRequest requestBody = new DiscoveryTestRequest();

        //Execute the request and add the sender names and numbers of device descriptions to the map
        sendRepositoryRequest(requestTopic, requestBody, new TypeReference<ReplyMessage<DiscoveryTestReply>>() {
        }).forEach(m -> repositoryMap.put(m.getSenderName(), m.getMessageBody().getDevicesCount()));

        //Return result map
        return repositoryMap;
    }

    private <Q extends DomainMessageBody, R extends DomainMessageBody> List<ReplyMessage<R>> sendRepositoryRequest(RequestTopic requestTopic, Q requestMessageBody, TypeReference<ReplyMessage<R>> replyTypeReference) {
        return sendRepositoryRequest(Collections.singletonList(requestTopic), requestMessageBody, replyTypeReference);
    }


    private <Q extends DomainMessageBody, R extends DomainMessageBody> List<ReplyMessage<R>> sendRepositoryRequest(Collection<RequestTopic> requestTopics, Q requestMessageBody, TypeReference<ReplyMessage<R>> replyTypeReference) {
        //Sanity checks
        if ((requestTopics == null) || (requestTopics.isEmpty()) || requestTopics.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The request topics must not be null or empty.");
        } else if (requestMessageBody == null) {
            throw new IllegalArgumentException("The request message body must not be null.");
        } else if (replyTypeReference == null) {
            throw new IllegalArgumentException("The reply message type reference must not be null.");
        }

        //Start building of a new scatter gather request
        ScatterGatherRequestBuilder requestBuilder = this.pubSubService.buildScatterGatherRequest();

        //Iterate over all provided request topics
        for (RequestTopic topic : requestTopics) {
            //Put request topic together
            String requestTopic = topic.getFullTopic() + TOPIC_SUFFIX_TEST_REQUEST;

            //Generate a new return topic by using the owner of the request topic
            String returnTopic = pubSubService.generateReturnTopic(topic.getOwner(), "discovery");

            //Create new request message from given message body and set return topic accordingly
            RequestMessage<Q> stageRequestMessage = new RequestMessage<Q>(requestMessageBody).setReturnTopic(returnTopic);

            //Create request stage config for this request topic
            RequestStageConfig<RequestMessage<Q>> stageConfig =
                    new DomainRequestStageConfig<>(requestTopic, stageRequestMessage)
                            .setTimeout(topic.getTimeout())
                            .setExpectedReplies(topic.getExpectedReplies());

            //Add stage config to request under construction
            requestBuilder.addRequestStage(stageConfig);
        }

        //Build scatter gather request
        ScatterGatherRequest<ReplyMessage<R>> scatterGatherRequest = requestBuilder.buildForDomain(replyTypeReference);

        //Execute request and collect and return the results
        return scatterGatherRequest.execute();
    }
}
