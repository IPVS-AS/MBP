/*
package de.ipvs.as.mbp.service.messaging.scatter_gather;

import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;
import org.json.JSONObject;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class ScatterGatherRequestBuilder {

    //TODO new class for request topic (ScatterGatherRequest) that consists out of topic, timeout and replies
    //TODO Check if AtomicReference is needed everywhere
    //TODO make sure unsubscription of topic works
    //TODO Use List instead of set (removes duplicates automatically :-( )
    //TODO Implement for JSON as well

    private final PubSubService pubSubService;

    public ScatterGatherRequestBuilder(PubSubService pubSubService) {
        this.pubSubService = pubSubService;
    }


    public ScatterGatherRequest<String> create(ScatterGatherConfig config, String replyTopicFilter, String requestMessage) {

    }

    public ScatterGatherRequest<JSONObject> createJSON(ScatterGatherConfig config, String replyTopicFilter, JSONObject requestMessage){

    }

    public ScatterGatherRequest<String> create(Collection<ScatterGatherConfig> configs, String replyTopicFilter, String requestMessage){
        CompletableFuture.allOf(configs.stream().map(c -> create(c, replyTopicFilter, requestMessage).getFuture()));
    }

    public ScatterGatherRequest<JSONObject> createJSON(Collection<ScatterGatherConfig> configs, String replyTopicFilter, JSONObject requestMessage){

    }


    private Supplier<List<String>> createRequestStage(){

    }

    private CompletableFuture<List<String>> createUnsubscribeStage(){

    }

    private CompletableFuture<List<JSONObject>> createJSONConversionStage(){

    }


    public CompletableFuture<Set<String>> scatterGatherOld(RequestTopic requestTopic, String replyTopicFilter, String requestMessage) {

        final AtomicReference<CompletableFuture<Set<String>>> futureReference = new AtomicReference<>();
        final AtomicReference<StringMessageListener> subscriberReference = new AtomicReference<>();

        CompletableFuture<Set<String>> completableFuture = CompletableFuture.supplyAsync(() -> {
            Set<String> replyMessages = new HashSet<>();

            StringMessageListener listener = (message, topic, topicFilter) -> {
                replyMessages.add(message);

                if ((replyMessages.size() >= requestTopic.getExpectedReplies())) {
                    futureReference.get().complete(replyMessages);
                }
            };
            subscriberReference.set(listener);
            pubSubService.subscribe(replyTopicFilter, listener);

            pubSubService.publish("requesttopic", requestMessage);

            try {
                Thread.sleep(requestTopic.getTimeout());
            } catch (InterruptedException ignored) {
            }

            return replyMessages;

        }).thenApply(messages -> {
            pubSubService.unsubscribe(replyTopicFilter, subscriberReference.get());
            return messages;
        });
        futureReference.set(completableFuture);
        return completableFuture;
    }

    public void scatterGatherOld(RequestTopic requestTopic, String replyTopicFilter, JSONObject requestMessage) {
        this.scatterGatherOld(requestTopic, replyTopicFilter, requestMessage.toString());
    }
}
*/