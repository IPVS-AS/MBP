package de.ipvs.as.mbp.service.log_writer;

import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import de.ipvs.as.mbp.repository.ValueLogRepository;
import de.ipvs.as.mbp.service.cep.trigger.CEPValueLogCache;
import de.ipvs.as.mbp.service.receiver.ValueLogObserver;
import de.ipvs.as.mbp.service.receiver.ValueLogReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service that registers itself as observer at the ValueLogReceiver and writes all arrived value logs
 * into the InfluxDB time series database.
 */
@Service
public class ValueLogWriter implements ValueLogObserver {

    //Repository component to use for storing value logs (autowired)
    private ValueLogRepository valueLogRepository;

    //Cache for value logs
    private CEPValueLogCache cepValueLogCache;

    /**
     * Creates and starts the service by passing references to a {@link ValueLogReceiver} service, the
     * {@link ValueLogRepository} that is supposed to be used for storing the received value logs in and
     * a {@link CEPValueLogCache} for caching the received value logs (auto-wired).
     *
     * @param valueLogReceiver   The instance of the {@link ValueLogReceiver} service to use
     * @param valueLogRepository The  {@link ValueLogRepository} to use
     * @param cepValueLogCache   The {@link CEPValueLogCache} to use
     */
    @Autowired
    public ValueLogWriter(ValueLogReceiver valueLogReceiver, ValueLogRepository valueLogRepository, CEPValueLogCache cepValueLogCache) {
        //Store references
        this.valueLogRepository = valueLogRepository;
        this.cepValueLogCache = cepValueLogCache;

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
        // Remove the value log from the CEPValueLogCache as it should be now written to the database
        cepValueLogCache.removeValueLog(valueLog);
    }
}