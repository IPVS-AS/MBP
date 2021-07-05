
package de.ipvs.as.mbp.service.messaging.scatter_gather;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.scatter_gather.config.RequestStageConfig;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.CorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.DomainCorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.JSONCorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.StringCorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.topics.ReturnTopicGenerator;
import de.ipvs.as.mbp.util.Json;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Objects of this class can be used in order to build {@link ScatterGatherRequest}s step by step. A scatter gather
 * request is able to synchronously publish user-defined request messages under one or even multiple topics and to
 * receive, process and collect the replies that are published by the receivers of the request messages under
 * corresponding return topics in response to the requests. Scatter gather requests can deal with different types
 * of reply messages (string, JSON or {@link DomainMessage} objects) and consist out of multiple stages where
 * each stage represents one request message that is published under one topic. Scatter gather request stages
 * are created from {@link RequestStageConfig}s which wrap all the information that is necessary in order to create the
 * stage. This also includes a timeout value and an expected number of replies which determine when the receiving
 * of reply messages to a preceding request message can be considered as complete for the corresponding request stage.
 * A scatter gather request terminates as soon as all request stages concluded successfully.
 * Across multiple request stages, the same return topic may be used for receiving the replies that are published in
 * response to the request messages. However, in order to avoid the processing of duplicated messages in this case,
 * a {@link CorrelationVerifier} should be set when building the {@link ScatterGatherRequest}. Such a correlation
 * verifier is responsible to decide whether an incoming reply message correlates with the {@link RequestStageConfig}
 * that resulted in a request stage for which the message was possibly received within the scatter gather request.
 * This can typically done by comparing correlation identifiers.
 */
public class ScatterGatherRequestBuilder {

    //Publish-subscribe-based messaging service to use
    private final PubSubService pubSubService;

    //Return topic generator to use
    private final ReturnTopicGenerator returnTopicGenerator;

    //Request stage configurations
    private final Set<RequestStageConfig<?>> requestStageConfigs;

    //Correlation verifier to use
    private CorrelationVerifier<?> correlationVerifier = null;

    /**
     * Creates a new scatter gather request builder from a given {@link PubSubService}, which is supposed to be
     * used for performing publish-subscribe-based messaging tasks, and a {@link ReturnTopicGenerator} that is able
     * to generate unique return topics.
     *
     * @param pubSubService        The publish-subscribe-based messaging service to use
     * @param returnTopicGenerator The return topic generator to use
     */
    public ScatterGatherRequestBuilder(PubSubService pubSubService, ReturnTopicGenerator returnTopicGenerator) {
        //Set provided dependencies
        this.pubSubService = pubSubService;
        this.returnTopicGenerator = returnTopicGenerator;

        //Create new set of request stage configurations
        this.requestStageConfigs = new HashSet<>();
    }


    /**
     * Sets the {@link CorrelationVerifier} of the scatter gather request under construction. The correlation verifier
     * is responsible to decide  whether an incoming reply message matches the {@link RequestStageConfig} for which
     * it was received, which can typically done by comparing correlation identifiers. This way, the processing of
     * duplicated messages can be avoided when the same return topic is used cross multiple scatter gather
     * request stages.
     *
     * @param correlationVerifier The correlation verifier to set
     * @return The scatter gather request builder for chaining
     */
    public ScatterGatherRequestBuilder setCorrelationVerifier(CorrelationVerifier<?> correlationVerifier) {
        //Set the correlation verifier (null means no verifier)
        this.correlationVerifier = correlationVerifier;

        //Return builder for chaining
        return this;
    }

    /**
     * Adds a new request stage, described by a given {@link RequestStageConfig}, to the scatter gather request under
     * construction.
     *
     * @param config The configuration of the request stage to use
     * @return The scatter gather request builder for chaining
     */
    public ScatterGatherRequestBuilder addRequestStage(RequestStageConfig<?> config) {
        //Sanity check
        if (config == null) {
            throw new IllegalArgumentException("The scatter gather configuration must not be null.");
        }

        //Add request stage configuration to set
        this.requestStageConfigs.add(config);

        //Return builder for chaining
        return this;
    }

    /**
     * Adds multiple request stages, described by a collection of {@link RequestStageConfig}s, to the scatter gather
     * request under construction.
     *
     * @param configs The collection of request stage configurations to use
     * @return The scatter gather request builder for chaining
     */
    public ScatterGatherRequestBuilder addRequestStages(Collection<RequestStageConfig<?>> configs) {
        //Sanity check
        if ((configs == null) || (configs.isEmpty()) || (configs.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The scatter gather configurations must not be null or none.");
        }

        //Add request stage configurations to set
        this.requestStageConfigs.addAll(configs);

        //Return builder for chaining
        return this;
    }

    /**
     * Finishes the building of the scatter gather request under construction by creating and returning a corresponding
     * {@link ScatterGatherRequest} object that is able to deal with string reply messages. The resulting request
     * can be synchronously executed at any time.
     *
     * @return The resulting scatter gather request
     */
    public ScatterGatherRequest<String> buildForString() {
        //Ensure that there are request stages
        requireRequestStages();

        //Check correlation verifier compatibility
        requireCompatibleCorrelationVerifier(this.correlationVerifier, StringCorrelationVerifier.class);

        //Create request stages for all available configs
        Set<CompletableFuture<List<String>>> requestStages = createRequestStages(this.correlationVerifier);

        //Combine all request stages of the set
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(requestStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, requestStages);
    }

    /**
     * Finishes the building of the scatter gather request under construction by creating and returning a corresponding
     * {@link ScatterGatherRequest} object that is able to deal with JSON reply messages. The resulting request
     * can be synchronously executed at any time.
     *
     * @return The resulting scatter gather request
     */
    public ScatterGatherRequest<JSONObject> buildForJSON() {
        //Ensure that there are request stages
        requireRequestStages();

        //Check correlation verifier compatibility
        requireCompatibleCorrelationVerifier(this.correlationVerifier, JSONCorrelationVerifier.class);

        //Create request stages for all available configs
        Set<CompletableFuture<List<String>>> requestStages = createRequestStages(this.correlationVerifier);

        //Extend all request stages for  transformation
        Set<CompletableFuture<List<JSONObject>>> transformedStages = requestStages.stream()
                .map(s -> s.thenApply(getJSONTransformation()))
                .collect(Collectors.toSet());

        //Combine all request stages
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(transformedStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, transformedStages);
    }

    /**
     * Finishes the building of the scatter gather request under construction by creating and returning a corresponding
     * {@link ScatterGatherRequest} object that is able to deal with {@link DomainMessage}s as reply messages.
     * The resulting request can be synchronously executed at any time.
     *
     * @param <R> The type of the reply domain message
     * @return The resulting scatter gather request
     */

    public <R extends DomainMessage<? extends DomainMessageBody>> ScatterGatherRequest<R> buildForDomain(TypeReference<R> typeReference) {
        //Ensure that there are request stages
        requireRequestStages();

        //Check correlation verifier compatibility
        requireCompatibleCorrelationVerifier(this.correlationVerifier, DomainCorrelationVerifier.class);

        //Create request stages for all available configs
        Set<CompletableFuture<List<String>>> requestStages = createRequestStages(this.correlationVerifier);

        //Extend all request stages for transformation
        Set<CompletableFuture<List<R>>> transformedStages = requestStages.stream()
                .map(s -> s.thenApply(getDomainMessageTransformation(typeReference)))
                .collect(Collectors.toSet());

        //Combine all request stages
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(transformedStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, transformedStages);
    }

    /**
     * Creates and returns request stages from the {@link RequestStageConfig}s which were previously added
     * to the request builder. Each request stage is represented by a executable {@link CompletableFuture} and includes
     * the publishing of the request message, the receiving of reply messages, their transformation, as well as
     * the unsubscription from the return topic after the receiving phase concluded. Optionally, a correlation verifier
     * can be passed which will then be used in order to retain only those messages which correlate with the
     * {@link RequestStageConfig} that resulted in a request stage for which the message was possibly received.
     *
     * @param correlationVerifier The correlation verifier to use or null if no correlation verifier should be used
     * @return The resulting set of completable futures that represent the individual created request stages
     */
    private Set<CompletableFuture<List<String>>> createRequestStages(CorrelationVerifier<?> correlationVerifier) {
        //Check for state
        requireRequestStages();

        //Stream through all configs and create request stages from them
        return this.requestStageConfigs.stream().map(c -> createRequestStage(c, correlationVerifier))
                .collect(Collectors.toSet());
    }

    /**
     * Creates and returns a request stage from a given {@link RequestStageConfig}. The request stage is represented
     * by a executable {@link CompletableFuture} and includes the publishing of the request message, the receiving of
     * reply messages, their transformation, as well as the unsubscription from the return topic after the receiving
     * phase concluded. Optionally, a correlation verifier can be passed that will then be used in order to retain
     * only those messages which correlate with the {@link RequestStageConfig} for which the request stage was created.
     *
     * @param config              The configuration to create the request stage from
     * @param correlationVerifier The correlation verifier to use or null if no correlation verifier should be used
     * @return The resulting completable future that represents the created request stage
     */
    private CompletableFuture<List<String>> createRequestStage(RequestStageConfig<?> config, CorrelationVerifier<?> correlationVerifier) {
        //Create atomic reference wrapper for the future object itself, the reply message listener and the return topic
        final AtomicReference<CompletableFuture<List<String>>> futureReference = new AtomicReference<>();
        final AtomicReference<StringMessageListener> replyListenerReference = new AtomicReference<>();

        //Create and store the completable future
        futureReference.set(CompletableFuture.supplyAsync(() -> {
            //Create result list for incoming reply messages
            List<String> replyMessages = new ArrayList<>();

            //Create listener for the incoming reply messages
            replyListenerReference.set((message, topic, topicFilter) -> {
                //Perform correlation verification if possible
                if (!isCorrelated(message, config, correlationVerifier)) {
                    //Message and config are not correlated, thus ignore message
                    return;
                }

                //Add received message to list
                replyMessages.add(message);

                //Check if number of received replies matches the number of expected ones
                if ((replyMessages.size() >= config.getExpectedReplies())) {
                    //Probably all replies received, thus terminate
                    futureReference.get().complete(replyMessages);
                }
            });

            //Subscribe listener to return topic
            pubSubService.subscribe(config.getReturnTopic(), replyListenerReference.get());

            //Transform request message to string and publish request
            pubSubService.publish(config.getRequestTopic(), config.getRequestMessage().toString());

            //Sleep until timeout
            try {
                Thread.sleep(config.getTimeout());
            } catch (InterruptedException ignored) {
            }

            //Return all received messages
            return replyMessages;
        }));

        //Add stage for unsubscription of the listener and return the result
        return futureReference.get().thenApply(messages -> {
            pubSubService.unsubscribe(config.getReturnTopic(), replyListenerReference.get());
            return messages;
        });
    }

    /**
     * Uses a given {@link CorrelationVerifier} in order to check whether a given message is correlated to a given
     * {@link RequestStageConfig} that resulted in a request stage for which the message was possibly received within
     * the scatter gather request. For this, the given message is transformed into a message type that matches
     * the type of the correlation verifier. If the provided correlation verifier is null, the correlation is always
     * assumed and thus true is returned.
     *
     * @param message             The message to compare
     * @param config              The configuration of the request stage to compare
     * @param correlationVerifier The correlation verifier to use
     * @return True, if the message is correlated to the configuration of the request stage; false otherwise
     */
    private boolean isCorrelated(String message, RequestStageConfig<?> config, CorrelationVerifier<?> correlationVerifier) {
        //Check if correlation verifier is provided
        if (correlationVerifier == null) {
            //No verifier, thus keep all messages
            return true;
        } else if (correlationVerifier instanceof DomainCorrelationVerifier) {
            //Domain message correlation verifier
            return ((DomainCorrelationVerifier<?>) correlationVerifier).isCorrelated(message, config);
        } else if (correlationVerifier instanceof StringCorrelationVerifier) {
            //String message correlation verifier
            return ((StringCorrelationVerifier) correlationVerifier).isCorrelated(message, config);
        } else if (correlationVerifier instanceof JSONCorrelationVerifier) {
            try {
                //Transform message to JSON object
                JSONObject jsonMessage = new JSONObject(message);
                return ((JSONCorrelationVerifier) correlationVerifier).isCorrelated(jsonMessage, config);
            } catch (JSONException e) {
                //Transformation to JSON failed, thus ignore message
                return false;
            }
        }

        //Invalid correlation verifier, thus keep all messages
        return true;
    }

    /**
     * Creates a function that is able to transform a given list of strings to a corresponding list of
     * {@link JSONObject}s of equal size. If the transformation fails for one string, null is added to the
     * result list instead.
     *
     * @return The resulting transformation function
     */
    private Function<List<String>, List<JSONObject>> getJSONTransformation() {
        //Stream the input list and map each element to a JSON object
        return strings -> strings.stream().map(s -> {
            try {
                //Transform string to JSON object
                return new JSONObject(s);
            } catch (JSONException e) {
                //Transformation failed
                return null;
            }
        }).collect(Collectors.toList());
    }

    /**
     * Creates a function that is able to transform a given list of strings to a corresponding list of
     * {@link DomainMessage} objects of equal size by using a given type reference. This type reference
     * described the exact type of the {@link DomainMessage} to which the strings are supposed to be transformed.
     * If the transformation fails for one string, null is added to the result list instead.
     *
     * @param typeReference The type reference describing the target type to use in the transformation
     * @param <R>           The type of the reply domain message
     * @return The resulting transformation function
     */
    private <R extends DomainMessage<? extends DomainMessageBody>> Function<List<String>, List<R>> getDomainMessageTransformation(TypeReference<R> typeReference) {
        //Stream through all string messages and transform them to domain message objects
        return strings -> strings.stream().map(s -> Json.toObject(s, typeReference)).collect(Collectors.toList());
    }

    /**
     * Takes a collection of scatter gather request stages, represented by {@link CompletableFuture}s, and combines
     * them into a single, overall request that executes the individual stages in parallel. The resulting,
     * overall request is a {@link CompletableFuture} that completes as soon as all individual request stages
     * complete successfully.
     *
     * @param requestStages The collection of individual request stages to create the overall request from
     * @return The resulting, overall request
     */
    private CompletableFuture<Void> combineRequestStages(Collection<CompletableFuture<?>> requestStages) {
        //Convert set of request stages to array
        CompletableFuture<?>[] stagesArray = new CompletableFuture[requestStages.size()];
        stagesArray = requestStages.toArray(stagesArray);

        //Combine all futures into one
        return CompletableFuture.allOf(stagesArray);
    }


    /**
     * Checks whether any request stages where added to the scatter gather request under construction.
     * If this is not the case, an exception will be thrown.
     */
    private void requireRequestStages() {
        //Check if there are any request stage configurations
        if (this.requestStageConfigs.isEmpty()) {
            throw new IllegalStateException("Request stages need to be added before the request can be build.");
        }
    }

    /**
     * Checks whether a given {@link CorrelationVerifier} is an instance of a given class. If this is not the case, an
     * exception will be thrown. This way, the compatibility of the correlation verifier to a certain message type
     * can be checked. If the provided correlation verifier is null, this is interpreted as no usage of a correlation
     * verifier and thus no exception will be thrown.
     *
     * @param verifier      The correlation verifier to check
     * @param verifierClass The class to check the correlation verifier against
     */
    private void requireCompatibleCorrelationVerifier(CorrelationVerifier<?> verifier, Class<?> verifierClass) {
        //Check for null
        if ((verifier == null) || (verifierClass == null)) {
            return;
        }

        //Check if correlation verifier is instance of the provided class
        if (!verifierClass.isInstance(verifier)) {
            throw new IllegalArgumentException(String.format("The correlation verifier for this build method must be of class %s, but is %s.", verifierClass.getName(), verifier.getClass().getName()));
        }
    }
}