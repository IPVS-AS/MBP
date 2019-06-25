package org.citopt.connde.service.log_writer;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.repository.ValueLogInfluxDBRepository;
import org.citopt.connde.service.receiver.ValueLogReceiver;
import org.citopt.connde.service.receiver.ValueLogReceiverObserver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that registers itself as observer at the ValueLogReceiver and writes all arrived value logs
 * into the InfluxDB time series database.
 *
 * //TODO Rename to "ValueLogWriter"
 */
@Service
public class ValueLogInfluxDBWriter implements ValueLogReceiverObserver {

    //Repository component to use for storing value logs
    private ValueLogInfluxDBRepository valueLogRepository;

    /**
     * Creates and starts the service by passing references to a value log receiver service
     * and the repository component that is supposed to be used for storing the received value logs in (auto-wired).
     *
     * @param valueLogReceiver   The instance of the value log receiver service
     * @param valueLogRepository The repository component to use
     */
    @Autowired
    public ValueLogInfluxDBWriter(ValueLogReceiver valueLogReceiver, ValueLogInfluxDBRepository valueLogRepository) {
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

        //Write value log into repository
        valueLogRepository.write(valueLog);
    }
}