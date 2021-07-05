package de.ipvs.as.mbp.service.discovery;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.domain.discovery.collections.DeviceDescriptionSet;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.messages.query.DeviceQueryReply;
import de.ipvs.as.mbp.domain.discovery.messages.query.DeviceQueryRequest;
import de.ipvs.as.mbp.domain.discovery.messages.test.RepositoryTestReply;
import de.ipvs.as.mbp.domain.discovery.messages.test.RepositoryTestRequest;
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

    //The publish-subscribe-based messaging service to use
    @Autowired
    private PubSubService pubSubService;

    /**
     * Creates and initializes the discovery gateway.
     */
    public DiscoveryGateway() {

    }

    /**
     * Requests device descriptions matching a given device template from the discovery repositories that are available
     * under a given collection of {@link RequestTopic}s. The device descriptions that are received from the discovery
     * repositories in response are returned as list of {@link DeviceDescriptionSet}s, containing one set
     * per repository.
     *
     * @param deviceTemplate The device template to query device descriptions for
     * @param requestTopics  The collection of {@link RequestTopic}s to use for sending the request to the repositories
     * @return The resulting list of {@link DeviceDescriptionSet}s
     */
    public List<DeviceDescriptionSet> getDevicesForTemplate(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("Device template must not be null.");
        }

        //Create the request message body
        DeviceQueryRequest requestBody = new DeviceQueryRequest()
                .setRequirements(deviceTemplate.getRequirements())  //Set requirements from device template
                .setScoringCriteria(deviceTemplate.getScoringCriteria()); //Set scoring criteria from device template

        //Execute the request and add the sender names and numbers of device descriptions to the map
        List<ReplyMessage<DeviceQueryReply>> replies = sendRepositoryRequest(requestTopics, requestBody, new TypeReference<ReplyMessage<DeviceQueryReply>>() {
        });

        //Stream the replies and create device descriptions sets from them
        return replies.stream().map(r -> new DeviceDescriptionSet(r.getSenderName(), deviceTemplate.getId())
                .addDeviceDescriptions(r.getMessageBody().getDeviceDescriptions())) //Add device descriptions
                .collect(Collectors.toList());
    }

    /**
     * Checks the availability of discovery repositories for a given {@link RequestTopic} and returns
     * a map (repository name --> device descriptions count) containing the unique names of the repositories
     * that replied to the request as well as the number of device descriptions they contain.
     *
     * @param requestTopic The request topic for which the repository availability is supposed to be tested
     * @return The resulting map (repository ID --> device descriptions count)
     */
    public Map<String, Integer> getAvailableRepositories(RequestTopic requestTopic) {
        //Sanity check
        if (requestTopic == null) {
            throw new IllegalArgumentException("Request topic must not be null.");
        }

        //Create result map
        Map<String, Integer> repositoryMap = new HashMap<>();

        //Create the request message body
        RepositoryTestRequest requestBody = new RepositoryTestRequest();

        //Execute the request and add the sender names and numbers of device descriptions to the map
        sendRepositoryRequest(requestTopic, requestBody, new TypeReference<ReplyMessage<RepositoryTestReply>>() {
        }).forEach(m -> repositoryMap.put(m.getSenderName(), m.getMessageBody().getDevicesCount()));

        //Return result map
        return repositoryMap;
    }

    /**
     * Creates and executes a scatter gather request for a given {@link RequestTopic}. Thereby, a request message with
     * a given {@link DomainMessageBody} of an arbitrary sub-type is published under the request topic and then the
     * corresponding replies are received and subsequently converted to {@link ReplyMessage}s of a given sub-type.
     * Finally, a list of these response messages is returned as result of this method call.
     * All in all, this method forms a easy-to-use framework for executing synchronous, discovery-related
     * scatter gather requests.
     *
     * @param requestTopic       The request topics to use
     * @param requestMessageBody The body to use in the request message
     * @param replyTypeReference References the desired type to which the reply messages are supposed to be transformed
     * @param <Q>                The type of the request message body
     * @param <R>                The type of the response message body
     * @return The resulting list of response messages in the desired type
     */
    private <Q extends DomainMessageBody, R extends DomainMessageBody> List<ReplyMessage<R>> sendRepositoryRequest(RequestTopic requestTopic, Q requestMessageBody, TypeReference<ReplyMessage<R>> replyTypeReference) {
        //Wrap request topic in list and call responsible method
        return sendRepositoryRequest(Collections.singletonList(requestTopic), requestMessageBody, replyTypeReference);
    }


    /**
     * Creates and executes a scatter gather request for a given collection of {@link RequestTopic}s. Thereby,
     * a request message with a given {@link DomainMessageBody} of an arbitrary sub-type is published under the request
     * topics and then the corresponding replies are received and subsequently converted to {@link ReplyMessage}s of
     * a given sub-type. Finally, a list of these response messages is returned as result of this method call.
     * All in all, this method forms a easy-to-use framework for executing synchronous, discovery-related
     * scatter gather requests.
     *
     * @param requestTopics      The collection of request topics to use
     * @param requestMessageBody The body to use in the request message
     * @param replyTypeReference References the desired type to which the reply messages are supposed to be transformed
     * @param <Q>                The type of the request message body
     * @param <R>                The type of the response message body
     * @return The resulting list of response messages in the desired type
     */
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
            //Create topic by using the RequestTopic and the suffix as specified by the message body
            String requestTopic = topic.getFullTopic() + "/" + requestMessageBody.getTopicSuffix();

            //Generate a new return topic by using the owner of the request topic
            String returnTopic = pubSubService.generateReturnTopic(topic.getOwner(), "discovery");

            //Create new request message from given message body and set return topic accordingly
            RequestMessage<Q> stageRequestMessage = new RequestMessage<>(requestMessageBody).setReturnTopic(returnTopic);

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
