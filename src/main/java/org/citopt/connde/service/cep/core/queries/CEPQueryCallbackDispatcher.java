package org.citopt.connde.service.cep.core.queries;

import org.citopt.connde.service.cep.core.output.CEPOutput;

import java.util.Map;

/**
 * Dispatcher for CEP query callbacks that converts the output to a CEPOutput object, creates a new thread
 * and notifies the subscriber within this thread.
 */
class CEPQueryCallbackDispatcher {
    //The subscriber that should be notified by the dispatcher
    private CEPQuerySubscriber subscriber;

    /**
     * Creates a new callback dispatcher by passing a dedicated subscriber that is supposed to be
     * notified in case of a callback.
     *
     * @param subscriber The subscriber
     */
    CEPQueryCallbackDispatcher(CEPQuerySubscriber subscriber) {
        setSubscriber(subscriber);
    }

    public void update(Map<String, Map<String, Object>> outputMap) {
        //Create object from output
        CEPOutput output = new CEPOutput(outputMap);

        Thread subscriberThread = new Thread(() -> subscriber.onQueryTriggered(output));
        subscriberThread.start();
    }

    /**
     * Returns the subscriber that is supposed to be notified by the dispatcher in case of a callback.
     *
     * @return The subscriber
     */
    public CEPQuerySubscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Sets the subscriber that is supposed to be notified by the dispatcher in case of a callback.
     *
     * @param subscriber The subscriber to set
     */
    public void setSubscriber(CEPQuerySubscriber subscriber) {
        //Sanity check
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber must not be null.");
        }
        this.subscriber = subscriber;
    }
}
