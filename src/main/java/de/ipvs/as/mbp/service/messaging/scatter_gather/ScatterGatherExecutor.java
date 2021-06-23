package de.ipvs.as.mbp.service.messaging.scatter_gather;

import de.ipvs.as.mbp.domain.discovery.topic.RequestTopic;
import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ScatterGatherExecutor {

    private final PubSubService pubSubService;

    public ScatterGatherExecutor(PubSubService pubSubService) {
        this.pubSubService = pubSubService;
    }

    public CompletableFuture<Set<String>> scatterGather(RequestTopic requestTopic, String replyTopicFilter, String requestMessage) {

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

    public void scatterGather(RequestTopic requestTopic, String replyTopicFilter, JSONObject requestMessage) {
        this.scatterGather(requestTopic, replyTopicFilter, requestMessage.toString());
    }
}
