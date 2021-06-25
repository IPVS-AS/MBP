
package de.ipvs.as.mbp.service.messaging.scatter_gather;

import com.fasterxml.jackson.core.type.TypeReference;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;
import de.ipvs.as.mbp.service.messaging.message.DomainMessage;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.CorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.DomainCorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.JSONCorrelationVerifier;
import de.ipvs.as.mbp.service.messaging.scatter_gather.correlation.StringCorrelationVerifier;
import de.ipvs.as.mbp.util.Json;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScatterGatherRequestBuilder {

    //Publish-subscribe-based messaging service to use
    private final PubSubService pubSubService;

    //Request stage configurations
    private final Set<RequestStageConfig<?>> requestStageConfigs;

    //Correlation verifier to use
    private CorrelationVerifier<?> correlationVerifier = null;

    public ScatterGatherRequestBuilder(PubSubService pubSubService) {
        //Set publish-subscribe-based messaging service
        this.pubSubService = pubSubService;

        //Create new set of request stage configurations
        this.requestStageConfigs = new HashSet<>();
    }


    public ScatterGatherRequestBuilder setCorrelationVerifier(CorrelationVerifier<?> correlationVerifier) {
        //Set the correlation verifier (null means no verifier)
        this.correlationVerifier = correlationVerifier;

        //Return builder for chaining
        return this;
    }

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

    public ScatterGatherRequest<String> buildForString() {
        //Ensure that there are request stages
        requireRequestStageConfigs();

        //Check correlation verifier compatibility
        requireCompatibleCorrelationVerifier(this.correlationVerifier, StringCorrelationVerifier.class);

        //Create request stages for all available configs
        Set<CompletableFuture<List<String>>> requestStages = createRequestStages(this.correlationVerifier);

        //Combine all request stages of the set
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(requestStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, requestStages);
    }

    public ScatterGatherRequest<JSONObject> buildForJSON() {
        //Ensure that there are request stages
        requireRequestStageConfigs();

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

    public ScatterGatherRequest<DomainMessage<? extends DomainMessageBody>> buildForDomain(TypeReference<? extends DomainMessage<? extends DomainMessageBody>> typeReference) {
        //Ensure that there are request stages
        requireRequestStageConfigs();

        //Check correlation verifier compatibility
        requireCompatibleCorrelationVerifier(this.correlationVerifier, DomainCorrelationVerifier.class);

        //Create request stages for all available configs
        Set<CompletableFuture<List<String>>> requestStages = createRequestStages(this.correlationVerifier);

        //Extend all request stages for transformation
        Set<CompletableFuture<List<DomainMessage<? extends DomainMessageBody>>>> transformedStages = requestStages.stream()
                .map(s -> s.thenApply(getDomainMessageTransformation(typeReference)))
                .collect(Collectors.toSet());

        //Combine all request stages
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(transformedStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, transformedStages);
    }

    private Set<CompletableFuture<List<String>>> createRequestStages(CorrelationVerifier<?> correlationVerifier) {
        //Check for state
        requireRequestStageConfigs();

        //Stream through all configs and create request stages from them
        return this.requestStageConfigs.stream().map(c -> createRequestStage(c, correlationVerifier))
                .collect(Collectors.toSet());
    }

    private CompletableFuture<List<String>> createRequestStage(RequestStageConfig<?> config, CorrelationVerifier<?> correlationVerifier) {
        //Create atomic reference wrapper for the future object itself and the reply message listener
        final AtomicReference<CompletableFuture<List<String>>> futureReference = new AtomicReference<>();
        final AtomicReference<StringMessageListener> replyListenerReference = new AtomicReference<>();

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

            //Subscribe listener to reply topic filter
            pubSubService.subscribe(config.getReplyTopicFilter(), replyListenerReference.get());

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
            pubSubService.unsubscribe(config.getReplyTopicFilter(), replyListenerReference.get());
            return messages;
        });
    }

    private Function<List<String>, List<JSONObject>> getJSONTransformation() {
        return strings -> strings.stream().map(s -> {
            try {
                return new JSONObject(s);
            } catch (JSONException e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    private Function<List<String>, List<DomainMessage<? extends DomainMessageBody>>> getDomainMessageTransformation(TypeReference<? extends DomainMessage<? extends DomainMessageBody>> typeReference) {
        //Stream through all string messages and transform them to domain message objects
        return strings -> strings.stream().map(s -> Json.toObject(s, typeReference)).collect(Collectors.toList());
    }

    private CompletableFuture<Void> combineRequestStages(Collection<CompletableFuture<?>> requestStages) {
        //Convert set of request stages to array
        CompletableFuture<?>[] stagesArray = new CompletableFuture[requestStages.size()];
        stagesArray = requestStages.toArray(stagesArray);

        //Combine all futures into one
        return CompletableFuture.allOf(stagesArray);
    }

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

    private void requireRequestStageConfigs() {
        //Check if there are any request stage configurations
        if (this.requestStageConfigs.isEmpty()) {
            throw new IllegalStateException("Request stages need to be added before the request can be build.");
        }
    }

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