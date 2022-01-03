package de.ipvs.as.mbp.service.receiver;

import de.ipvs.as.mbp.domain.valueLog.ValueLog;

/**
 * Interface for components that observe the {@link ValueLogReceiver} and want to get notified in case
 * a new value log message arrives at the MBP.
 */
public interface ValueLogObserver {
    /**
     * Called when a new value log message arrives at the {@link ValueLogReceiver}. The message is then processed,
     * transformed and passed to this method as {@link ValueLog}.
     *
     * @param valueLog The resulting value log of the arrived message
     */
    void onValueReceived(ValueLog valueLog);
}