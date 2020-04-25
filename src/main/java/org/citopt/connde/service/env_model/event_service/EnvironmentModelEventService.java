package org.citopt.connde.service.env_model.event_service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
public class EnvironmentModelEventService {
    //Time after which the emitter times out
    private static final Long EMITTER_TIMEOUT = 30 * 60 * 1000L;

    //Subscription map (model id -> set of subscribers)
    private Map<String, Set<SseEmitter>> subscriptions;

    /**
     * Initializes the environment model event service.
     */
    public EnvironmentModelEventService() {
        subscriptions = new HashMap<>();
    }

    /**
     * Creates a new emitter, subscribes it to the model of a given ID and returns it.
     *
     * @param modelId The ID of the model to subscribe
     * @return The SSE emitter that is supposed to be returned to the client
     */
    public SseEmitter subscribe(String modelId) {
        //Sanity check
        if ((modelId == null) || (modelId.isEmpty())) {
            throw new IllegalArgumentException("Model ID must not be null or empty.");
        }

        //Create new emitter
        SseEmitter emitter = createEmitter(modelId);

        //Check if somebody already subscribed to this model
        if (subscriptions.containsKey(modelId)) {
            //Get set of subscribers and add emitter to set
            Set<SseEmitter> subscriberSet = subscriptions.get(modelId);
            subscriberSet.add(emitter);
        } else {
            //Add model with new set to subscription map
            Set<SseEmitter> subscriberSet = new HashSet<>();
            subscriberSet.add(emitter);
            subscriptions.put(modelId, subscriberSet);
        }

        return emitter;
    }

    public void publishEvent(String modelId) {
        //Sanity check
        if ((modelId == null) || (modelId.isEmpty())) {
            throw new IllegalArgumentException("Model ID must not be null or empty.");
        }

        //Check if somebody subscribed to the model
        if (!subscriptions.containsKey(modelId)) {
            return;
        }

        //Get set of subscribers for this model
        Set<SseEmitter> subscribers = subscriptions.get(modelId);

        //Create event
        SseEmitter.SseEventBuilder event = SseEmitter.event()
                .data("asdf")
                .name("asdf");

        //Iterate over all subscribers and publish the event
        for (SseEmitter subscriber : subscribers) {
            try {
                subscriber.send(event);
            } catch (IOException e) {
                //Unsubscribe on exception
                unsubscribe(modelId, subscriber);
            }
        }
    }

    /**
     * Unsubscribes an emitter from a model.
     *
     * @param modelId The ID of the model to unsubscribe from
     * @param emitter The emitter to unsubscribe
     */
    private void unsubscribe(String modelId, SseEmitter emitter) {
        //Sanity check
        if ((modelId == null) || (modelId.isEmpty())) {
            throw new IllegalArgumentException("Model ID must not be null or empty.");
        } else if (emitter == null) {
            throw new IllegalArgumentException("Emitter must not be null.");
        }

        //Ckeck if somebody subscribed to the model
        if (!subscriptions.containsKey(modelId)) {
            return;
        }

        //Remove emitter from subscription set
        subscriptions.get(modelId).remove(emitter);
    }

    /**
     * Creates, configures and returns a new SSE emitter object for a certain model.
     *
     * @param modelId The ID of the model to create the emitter for
     * @return The ready SSE emitter object
     */
    private SseEmitter createEmitter(String modelId) {
        //Sanity check
        if ((modelId == null) || (modelId.isEmpty())) {
            throw new IllegalArgumentException("Model ID must not be null or empty.");
        }

        //Create new emitter with timeout
        SseEmitter emitter = new SseEmitter(EMITTER_TIMEOUT);

        //Unsubscribe on timeout or completion
        Runnable unsubscribe = () -> {
            this.unsubscribe(modelId, emitter);
        };
        emitter.onCompletion(unsubscribe);
        emitter.onTimeout(unsubscribe);

        //Return configured emitter
        return emitter;
    }
}
