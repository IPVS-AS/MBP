package org.citopt.connde.service.cep.core.queries;

import org.citopt.connde.service.cep.core.queries.output.CEPOutput;

import java.util.Map;

class CEPQueryCallbackDispatcher {
    private CEPQuerySubscriber subscriber;

    CEPQueryCallbackDispatcher(CEPQuerySubscriber subscriber) {
        setSubscriber(subscriber);
    }

    public void update(Map<String, Map<String, Object>> outputMap) {
        //Create object from output
        CEPOutput output = new CEPOutput(outputMap);

        Thread subscriberThread = new Thread(() -> subscriber.onEventTriggered(output));
        subscriberThread.start();
    }

    public CEPQuerySubscriber getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(CEPQuerySubscriber subscriber) {
        //Sanity check
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber must not be null.");
        }
        this.subscriber = subscriber;
    }
}
