
package de.ipvs.as.mbp.service.messaging.scatter_gather;

import de.ipvs.as.mbp.service.messaging.PubSubService;
import de.ipvs.as.mbp.service.messaging.dispatcher.listener.StringMessageListener;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ScatterGatherRequestBuilder {

    //TODO make sure unsubscription of topic works
    //TODO Use List instead of set (removes duplicates automatically :-( )

    private final PubSubService pubSubService;

    public ScatterGatherRequestBuilder(PubSubService pubSubService) {
        this.pubSubService = pubSubService;
    }

    public ScatterGatherRequest<String> create(ScatterGatherConfig config) {
        //Sanity check
        if (config == null) {
            throw new IllegalArgumentException("The scatter gather configuration must not be null.");
        }

        //Create the request
        return create(Collections.singletonList(config));
    }


    public ScatterGatherRequest<String> create(Collection<ScatterGatherConfig> configs) {
        //Sanity check
        if ((configs == null) || (configs.isEmpty()) || (configs.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The scatter gather configurations must not be null or none.");
        }

        //Create one request stage per provided config
        List<CompletableFuture<List<String>>> individualFutures = configs.stream().map(this::createRequestStage).collect(Collectors.toList());

        //Convert list of futures to array
        CompletableFuture<?>[] futuresArray = new CompletableFuture[individualFutures.size()];
        futuresArray = individualFutures.toArray(futuresArray);

        //Combine all futures into one
        CompletableFuture<Void> overallFuture = CompletableFuture.allOf(futuresArray);

        //Wrap all futures into one request object
        return new ScatterGatherRequest<>(overallFuture, individualFutures);
    }


    private CompletableFuture<List<String>> createRequestStage(ScatterGatherConfig config) {
        //Create atomic reference wrapper for the future object itself and the reply message listener
        final AtomicReference<CompletableFuture<List<String>>> futureReference = new AtomicReference<>();
        final AtomicReference<StringMessageListener> replyListenerReference = new AtomicReference<>();

        futureReference.set(CompletableFuture.supplyAsync(() -> {
            //Create result list for incoming reply messages
            List<String> replyMessages = new ArrayList<>();

            //Create listener for the incoming reply messages
            replyListenerReference.set((message, topic, topicFilter) -> {
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
}