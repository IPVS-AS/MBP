package de.ipvs.as.mbp.service.cep.engine.esper;

import de.ipvs.as.mbp.service.cep.engine.core.output.CEPOutput;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQuerySubscriber;

import java.util.Map;

/**
 * Dispatcher for CEP query callbacks that converts the query result to a CEPOutput object, creates a new thread
 * and notifies the subscriber within this thread.
 */
class EsperCEPQueryDispatcher {
    //The subscriber that should be notified by the dispatcher
    private CEPQuerySubscriber subscriber;

    /**
     * Creates a new callback dispatcher by passing a dedicated subscriber that is supposed to be
     * notified in case of a callback.
     *
     * @param subscriber The subscriber
     */
    EsperCEPQueryDispatcher(CEPQuerySubscriber subscriber) {
        setSubscriber(subscriber);
    }

    /**
     * Called in case the dedicated CEP query matches the event stream.
     *
     * @param resultMap The result of the CEP query
     */
    public void update(Map<Object, Object> resultMap) {
        //Create object from result
        CEPOutput result = new CEPOutput(resultMap);

        //Create new thre
        // ad so that Esper is not blocked
        Thread subscriberThread = new Thread(() -> subscriber.onQueryTriggered(result));
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
