package de.ipvs.as.mbp.service.discovery;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestReply;
import de.ipvs.as.mbp.domain.discovery.messages.test.DiscoveryTestRequest;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.types.ReplyMessage;
import de.ipvs.as.mbp.service.messaging.message.types.RequestMessage;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequest;
import de.ipvs.as.mbp.service.messaging.scatter_gather.ScatterGatherRequestBuilder;
import de.ipvs.as.mbp.service.messaging.scatter_gather.config.DomainRequestStageConfig;
import de.ipvs.as.mbp.service.messaging.scatter_gather.config.RequestStageConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public Map<String, Integer> checkAvailableRepositories(RequestTopic requestTopic) {
        //TODO Sanity checks

        //Create request message body
        DiscoveryTestRequest requestBody = new DiscoveryTestRequest();

        //Execute the request
        List<DiscoveryTestReply> replies = sendRepositoryRequest(requestTopic, requestBody, new TypeReference<ReplyMessage<DiscoveryTestReply>>() {
        });

        return null;
    }

    private <Q extends DomainMessageBody, R extends DomainMessageBody> List<R> sendRepositoryRequest(RequestTopic requestTopic, Q requestMessageBody, TypeReference<ReplyMessage<R>> replyTypeReference) {
        return sendRepositoryRequest(Collections.singletonList(requestTopic), requestMessageBody, replyTypeReference);
    }


    private <Q extends DomainMessageBody, R extends DomainMessageBody> List<R> sendRepositoryRequest(Collection<RequestTopic> requestTopics, Q requestMessageBody, TypeReference<ReplyMessage<R>> replyTypeReference) {
        //TODO sanity checks

        //Start building of a new scatter gather request
        ScatterGatherRequestBuilder requestBuilder = this.pubSubService.buildScatterGatherRequest();

        //Iterate over all provided request topics
        for (RequestTopic topic : requestTopics) {
            //Generate a new return topic by using the owner of the request topic
            String returnTopic = pubSubService.generateReturnTopic(topic.getOwner(), "discovery");

            //Create new request message from given message body and set return topic accordingly
            RequestMessage<Q> stageRequestMessage = new RequestMessage<Q>(requestMessageBody).setReturnTopic(returnTopic);

            //Create request stage config for this request topic
            RequestStageConfig<RequestMessage<Q>> stageConfig =
                    new DomainRequestStageConfig<>(topic.getFullTopic() + TOPIC_SUFFIX_TEST_REQUEST, stageRequestMessage)
                            .setTimeout(topic.getTimeout())
                            .setExpectedReplies(topic.getExpectedReplies());

            //Add stage config to request under construction
            requestBuilder.addRequestStage(stageConfig);
        }

        //Build scatter gather request
        ScatterGatherRequest<ReplyMessage<R>> scatterGatherRequest = requestBuilder.buildForDomain(replyTypeReference);

        //Execute request and collect results
        List<ReplyMessage<R>> requestResults = scatterGatherRequest.execute();

        //Stream result list and return only the message bodies
        return requestResults.stream().map(DomainMessage::getMessageBody).collect(Collectors.toList());
    }
}
