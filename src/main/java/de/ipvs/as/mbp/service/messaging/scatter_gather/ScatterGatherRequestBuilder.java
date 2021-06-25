
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

    private final PubSubService pubSubService;
    private final Set<CompletableFuture<List<String>>> requestStages;

    public ScatterGatherRequestBuilder(PubSubService pubSubService) {
        //Set publish-subscribe-based messaging service
        this.pubSubService = pubSubService;

        //Create new set of request stages
        this.requestStages = new HashSet<>();
    }

    public ScatterGatherRequestBuilder addRequestStage(RequestStageConfig config) {
        return addRequestStage(config, null);
    }

    public ScatterGatherRequestBuilder addRequestStage(RequestStageConfig config, CorrelationVerifier<?> correlationVerifier) {
        //Sanity check
        if (config == null) {
            throw new IllegalArgumentException("The scatter gather configuration must not be null.");
        }

        //Create the request stage and add it to the set
        this.requestStages.add(createRequestStage(config, correlationVerifier));

        //Return builder for chaining
        return this;
    }

    public ScatterGatherRequestBuilder addRequestStages(Collection<RequestStageConfig> configs) {
        return addRequestStages(configs, null);
    }

    public ScatterGatherRequestBuilder addRequestStages(Collection<RequestStageConfig> configs, CorrelationVerifier<?> correlationVerifier) {
        //Sanity check
        if ((configs == null) || (configs.isEmpty()) || (configs.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The scatter gather configurations must not be null or none.");
        }

        //Create request stages for all configs and add them to the set
        configs.forEach(c -> this.requestStages.add(createRequestStage(c, correlationVerifier)));

        //Return builder for chaining
        return this;
    }

    public ScatterGatherRequest<String> buildForString() {
        //Ensure that there are request stages
        requireRequestStages();

        //Combine all request stages of the set
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(this.requestStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, this.requestStages);
    }

    public ScatterGatherRequest<JSONObject> buildForJSON() {
        //Ensure that there are request stages
        requireRequestStages();

        //Extend all request stages for  transformation
        Set<CompletableFuture<List<JSONObject>>> transformedStages = this.requestStages.stream()
                .map(s -> s.thenApply(getJSONTransformation()))
                .collect(Collectors.toSet());

        //Combine all request stages
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(transformedStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, transformedStages);
    }

    public ScatterGatherRequest<DomainMessage<? extends DomainMessageBody>> buildForDomain(TypeReference<? extends DomainMessage<? extends DomainMessageBody>> typeReference) {
        //Ensure that there are request stages
        requireRequestStages();

        //Extend all request stages for transformation
        Set<CompletableFuture<List<DomainMessage<? extends DomainMessageBody>>>> transformedStages = this.requestStages.stream()
                .map(s -> s.thenApply(getDomainMessageTransformation(typeReference)))
                .collect(Collectors.toSet());

        //Combine all request stages
        CompletableFuture<Void> overallFuture = combineRequestStages(new HashSet<>(transformedStages));

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, transformedStages);
    }

    private CompletableFuture<List<String>> createRequestStage(RequestStageConfig config, CorrelationVerifier<?> correlationVerifier) {
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

            //Publish request
            pubSubService.publish(config.getRequestTopic(), config.getRequestMessage());

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
        return strings -> strings.stream().map(s -> {
            try {
                return Json.MAPPER.readValue(s, typeReference);
            } catch (Exception e) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    private CompletableFuture<Void> combineRequestStages(Collection<CompletableFuture<?>> requestStages) {
        //Convert set of request stages to array
        CompletableFuture<?>[] stagesArray = new CompletableFuture[requestStages.size()];
        stagesArray = requestStages.toArray(stagesArray);

        //Combine all futures into one
        return CompletableFuture.allOf(stagesArray);
    }

    private boolean isCorrelated(String message, RequestStageConfig config, CorrelationVerifier<?> correlationVerifier) {
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

    private void requireRequestStages() {
        //Check if there are any request stages
        if (this.requestStages.isEmpty()) {
            throw new IllegalStateException("Request stages need to be added before the request can be build.");
        }
    }
}