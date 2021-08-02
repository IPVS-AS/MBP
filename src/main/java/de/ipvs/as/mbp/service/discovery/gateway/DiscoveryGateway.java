package de.ipvs.as.mbp.service.discovery.gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesContainer;
import de.ipvs.as.mbp.domain.discovery.collections.revision.CandidateDevicesRevision;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.messages.cancel.CancelSubscriptionsMessage;
import de.ipvs.as.mbp.domain.discovery.messages.query.CandidateDevicesReply;
import de.ipvs.as.mbp.domain.discovery.messages.query.CandidateDevicesRequest;
import de.ipvs.as.mbp.domain.discovery.messages.test.RepositoryTestReply;
import de.ipvs.as.mbp.domain.discovery.messages.test.RepositoryTestRequest;
import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.DomainMessageListener;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.types.CommandMessage;
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

    //Map (device template ID --> subscription) of subscriptions for candidate devices on behalf of device templates
    private final Map<String, CandidateDevicesSubscription> candidateDeviceSubscriptions;

    //Map (device template owner ID --> return topic) of return topics to use for receiving notifications
    private final Map<String, String> subscriptionReturnTopics;

    //Define global listener for incoming asynchronous notifications as resulting from subscriptions
    private final DomainMessageListener<ReplyMessage<CandidateDevicesReply>> subscriptionNotificationListener =
            new DomainMessageListener<>(new TypeReference<ReplyMessage<CandidateDevicesReply>>() {
            },
                    this::dispatchSubscriptionNotifications);

    //The publish-subscribe-based messaging service to use
    @Autowired
    private PubSubService pubSubService;

    /**
     * Creates and initializes the discovery gateway.
     */
    public DiscoveryGateway() {
        //Initialize data structures
        this.candidateDeviceSubscriptions = new HashMap<>();
        this.subscriptionReturnTopics = new HashMap<>();
    }

    /**
     * Requests {@link DeviceDescription}s of suitable candidate devices, matching a given {@link DeviceTemplate},
     * from the discovery repositories that are available under a given collection of {@link RequestTopic}s.
     * The retrieved {@link DeviceDescription}s of the candidate devices are subsequently returned as
     * {@link CandidateDevicesContainer} by this method, holding one {@link CandidateDevicesCollection}
     * per discovery repository. No subscription is created at the discovery repositories as part of this request.
     *
     * @param deviceTemplate The {@link DeviceTemplate} for which suitable candidate devices are supposed
     *                       to be retrieved
     * @param requestTopics  The collection of {@link RequestTopic}s to use for sending the request message to the
     *                       discovery repositories
     * @return The resulting {@link CandidateDevicesContainer}
     */
    public CandidateDevicesContainer getDeviceCandidates(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics) {
        //Delegate call to overloaded method
        return getCandidateDevicesWithSubscription(deviceTemplate, requestTopics, null);
    }

    /**
     * Requests {@link DeviceDescription}s of suitable candidate devices, matching a given {@link DeviceTemplate},
     * from the discovery repositories that are available under a given collection of {@link RequestTopic}s.
     * The retrieved {@link DeviceDescription}s of the candidate devices are subsequently returned as
     * {@link CandidateDevicesContainer} by this method, holding one {@link CandidateDevicesCollection} per discovery
     * repository. In addition, an asynchronous subscription can be created at the discovery repositories such that the
     * MBP becomes notified when the collection of suitable candidate devices changes over time for the given
     * {@link DeviceTemplate}.
     *
     * @param deviceTemplate The {@link DeviceTemplate} for which suitable candidate devices are supposed
     *                       to be retrieved
     * @param requestTopics  The collection of {@link RequestTopic}s to use for sending the request message to the
     *                       discovery repositories
     * @param subscriber     The {@link CandidateDevicesSubscriber} to notify about changes in the collection of
     *                       suitable candidate devices. If null, no subscription will be created.
     * @return The resulting {@link CandidateDevicesContainer}
     */
    public CandidateDevicesContainer getCandidateDevicesWithSubscription(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics, CandidateDevicesSubscriber subscriber) {
        //Null check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Create the body of the corresponding request message
        CandidateDevicesRequest requestBody = new CandidateDevicesRequest(deviceTemplate.getId(),
                deviceTemplate.getRequirements(), deviceTemplate.getScoringCriteria(),
                createSubscription(deviceTemplate, requestTopics, subscriber));

        //Execute the request
        List<ReplyMessage<CandidateDevicesReply>> replies = sendRepositoryRequest(requestTopics, requestBody, new TypeReference<ReplyMessage<CandidateDevicesReply>>() {
        });

        //Create new candidate devices container
        CandidateDevicesContainer container = new CandidateDevicesContainer();

        //Iterate over all received replies and create candidate device collections from the device descriptions
        List<CandidateDevicesCollection> collections = replies.stream()
                .map(m -> new CandidateDevicesCollection(m.getSenderName()).addCandidateDevices(m.getMessageBody().getFirstDeviceDescriptions()))
                .collect(Collectors.toList());

        //Create and return a candidate devices container of the received collections
        return new CandidateDevicesContainer(deviceTemplate.getId(), collections);
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
     * Internally registers a discovery repository subscription from a given {@link DeviceTemplate}, a
     * {@link Collection} of {@link RequestTopic}s and a {@link CandidateDevicesSubscriber} that is supposed to
     * receive the notifications originating from the subscription. In addition, this method generates and returns
     * a notification topic under which the notification messages by the discovery repository are expected to be
     * published. If no {@link CandidateDevicesSubscriber} is provided, it is assumed that no subscription
     * is supposed to be created.
     *
     * @param deviceTemplate The {@link DeviceTemplate} for which the subscription is created
     * @param requestTopics  The collection of request topics under which the subscription request is published
     * @param subscriber     The subscriber to notify about notification messages resulting from the subscription
     * @return The resulting notification topic or null if no {@link CandidateDevicesSubscriber} is provided
     */
    private String createSubscription(DeviceTemplate deviceTemplate, Collection<RequestTopic> requestTopics, CandidateDevicesSubscriber subscriber) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        } else if (subscriber == null) {
            return null;
        }

        //Get owner of the device template
        User deviceTemplateOwner = deviceTemplate.getOwner();

        //Create subscription object
        CandidateDevicesSubscription subscriptionObject = new CandidateDevicesSubscription(deviceTemplate, requestTopics, subscriber);

        //Check if there are already subscriptions for this owner
        if (subscriptionReturnTopics.containsKey(deviceTemplateOwner.getId())) {
            //Add the subscription to the map and override possible existing subscriptions for this device template
            this.candidateDeviceSubscriptions.put(deviceTemplate.getId(), subscriptionObject);
        } else {
            //Generate new return topic
            String returnTopic = this.pubSubService.generateReturnTopic(deviceTemplateOwner, "discovery");

            //Subscribe to the return topic and handle
            this.pubSubService.subscribeDomain(returnTopic, this.subscriptionNotificationListener);

            //Add subscription and return topic to the corresponding maps
            candidateDeviceSubscriptions.put(deviceTemplate.getId(), subscriptionObject);
            subscriptionReturnTopics.put(deviceTemplateOwner.getId(), returnTopic);
        }

        //Retrieve return topic
        return subscriptionReturnTopics.get(deviceTemplateOwner.getId());
    }

    /**
     * Cancels the subscription for a given device template. As a result, the originally registered subscriber
     * will not become notified anymore when the collection of suitable candidate devices changes over time
     * at a certain repository for the given {@link DeviceTemplate}. Optionally, a collection of additional
     * {@link RequestTopic}s can be passed in order to cancel subscriptions at currently unknown repositories
     * that are available under different {@link RequestTopic}s as well.
     *
     * @param deviceTemplate          The device template for which the subscription is supposed to be cancelled
     * @param additionalRequestTopics Optionally, the additional request topics to use for cancelling subscriptions
     */
    public void cancelSubscription(DeviceTemplate deviceTemplate, Collection<RequestTopic> additionalRequestTopics) {
        //Sanity checks
        if (deviceTemplate == null) throw new IllegalArgumentException("The device template must not be null.");

        //Remove subscription from map, but remember the old subscription object
        CandidateDevicesSubscription subscriptionObject = this.candidateDeviceSubscriptions.remove(deviceTemplate.getId());

        //Create set for all request topics under which the cancel message is supposed to be published
        Set<RequestTopic> requestTopics = new HashSet<>();

        //Add additional topics
        if (additionalRequestTopics != null) {
            requestTopics.addAll(additionalRequestTopics.stream().filter(Objects::nonNull).collect(Collectors.toSet()));
        }

        //Add topics from old subscription (if available)
        if ((subscriptionObject != null) && (subscriptionObject.getRequestTopics() != null)) {
            requestTopics.addAll(subscriptionObject.getRequestTopics());
        }

        //Create message for cancelling the subscription
        CancelSubscriptionsMessage messageBody = new CancelSubscriptionsMessage(deviceTemplate.getId());
        CommandMessage<CancelSubscriptionsMessage> cancelSubscriptionsMessage = new CommandMessage<>(messageBody);

        //Put topics together for publishing the cancel message
        List<String> cancelTopics = requestTopics.stream()
                .map(t -> t.getFullTopic() + "/" + messageBody.getTopicSuffix()).collect(Collectors.toList());

        //Publish the cancel message
        this.pubSubService.publish(cancelTopics, cancelSubscriptionsMessage);

        //Check whether a subscription existed
        if (subscriptionObject == null) {
            return;
        }

        //Get owner ID of the device template
        String ownerId = deviceTemplate.getOwner().getId();

        //Check whether there are remaining subscriptions for this owner
        if (this.candidateDeviceSubscriptions.values().stream().anyMatch(s -> ownerId.equals(s.getDeviceTemplate().getOwner().getId()))) {
            //There are other subscriptions for this owner, so do nothing
            return;
        }

        //No subscriptions remain for this owner, so entirely unsubscribe from the topic
        this.pubSubService.unsubscribe(this.subscriptionReturnTopics.remove(ownerId), this.subscriptionNotificationListener);
    }

    /**
     * Cancels the subscriptions for a collection of {@link DeviceTemplate}s at the discovery repositories that are
     * available under a given {@link RequestTopic}, without cancelling the subscriptions at discovery repositories
     * for other {@link RequestTopic}s.
     *
     * @param deviceTemplates The collection of {@link DeviceTemplate}s for which the subscriptions are supposed to
     *                        be cancelled
     * @param requestTopic    The {@link RequestTopic} under which the pertaining discovery repositories are available
     */
    public void cancelSubscriptionsForRequestTopic(Collection<DeviceTemplate> deviceTemplates, RequestTopic requestTopic) {
        //Null checks
        if ((deviceTemplates == null) || deviceTemplates.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The device templates must not be null.");
        } else if (requestTopic == null) {
            throw new IllegalArgumentException("The request topic must not be null.");
        }

        //Create message for cancelling the subscriptions
        CancelSubscriptionsMessage messageBody = new CancelSubscriptionsMessage(deviceTemplates.stream().map(DeviceTemplate::getId).collect(Collectors.toSet()));
        CommandMessage<CancelSubscriptionsMessage> cancelSubscriptionsMessage = new CommandMessage<>(messageBody);

        //Publish the cancel message
        this.pubSubService.publish(requestTopic.getFullTopic() + "/" + messageBody.getTopicSuffix(), cancelSubscriptionsMessage);
    }

    /**
     * Returns whether there is an existing subscription for the given {@link DeviceTemplate}.
     *
     * @param deviceTemplate The device template to check
     * @return True, if an subscription exists for the device template; false otherwise
     */
    public boolean isSubscribed(DeviceTemplate deviceTemplate) {
        //Sanity check
        if (deviceTemplate == null) {
            throw new IllegalArgumentException("The device template must not be null.");
        }

        //Check whether a subscription exists
        return this.candidateDeviceSubscriptions.containsKey(deviceTemplate.getId());
    }

    /**
     * Dispatches the notifications that are received as result of repository subscriptions to the corresponding
     * subscribers of the pertained {@link DeviceTemplate}s. This way, the subscribers are asynchronously notified
     * when the collection of suitable candidate devices for the {@link DeviceTemplate} changes over time at a certain
     * repository.
     *
     * @param message     The received notification message
     * @param topic       The topic under which the message was received
     * @param topicFilter The topic filter that was used for subscribing to the notification message at the
     *                    publish-subscribe-based messaging broker.
     */
    private void dispatchSubscriptionNotifications(ReplyMessage<CandidateDevicesReply> message, String topic, String topicFilter) {
        //Unpack the received message
        String senderName = message.getSenderName();
        Set<CandidateDevicesRevision> revisions = message.getMessageBody().getCandidateDevicesRevisions();

        //Iterate over the received revisions
        revisions.forEach(revision -> {
            //Stream through the IDs of the pertaining device templates
            revision.getReferenceIds().stream()
                    .filter(Objects::nonNull)
                    .filter(this.candidateDeviceSubscriptions::containsKey)
                    .forEach(id -> {
                        //Get subscription for this device template
                        CandidateDevicesSubscription subscription = this.candidateDeviceSubscriptions.get(id);

                        //Notify the subscriber about the update
                        subscription.getSubscriber().onCandidateDevicesChanged(id, senderName, revision);
                    });
        });
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
