package org.citopt.connde.service.receiver;

import org.citopt.connde.domain.valueLog.ValueLog;

/**
 * Interface for components that observe the ValueReceiver and want to get notified in case
 * a new value message arrives.
 */
public interface ValueLogReceiverObserver {
    /**
     * Called in case a new value message arrives at the ValueLogReceiver. The transformed message is passed
     * as value log.
     *
     * @param valueLog The corresponding value log that arrived
     */
    void onValueReceived(ValueLog valueLog);
}
