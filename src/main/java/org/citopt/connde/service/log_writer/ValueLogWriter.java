package org.citopt.connde.service.log_writer;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ValueLogRepository;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.citopt.connde.service.receiver.ValueLogReceiverObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that registers itself as observer at the ValueLogReceiver and writes all arrived value logs
 * into the dedicated value log repository.
 */
@Service
public class ValueLogWriter implements ValueLogReceiverObserver {
    //Repository for value logs (auto-wired)
    private ValueLogRepository valueLogRepository;

    /**
     * Creates and starts the ValueLogWriter service by passing a value log receiver service instance
     * and the value log repository in which the value logs are supposed to be stored (auto-wired).
     *
     * @param valueLogReceiver   The instance of the value log receiver service
     * @param valueLogRepository The value log repository
     */
    @Autowired
    public ValueLogWriter(ValueLogReceiver valueLogReceiver, ValueLogRepository valueLogRepository) {
        this.valueLogRepository = valueLogRepository;

        //Register as observer at the ValueLogReceiver
        valueLogReceiver.registerObserver(this);
    }

    /**
     * Called in case a new value message arrives at the ValueLogReceiver. The transformed message is passed
     * as value log.
     *
     * @param valueLog The corresponding value log that arrived
     */
    @Override
    public void onValueReceived(ValueLog valueLog) {
        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }

        //Insert value log into repository
        valueLogRepository.insert(valueLog);
    }
}