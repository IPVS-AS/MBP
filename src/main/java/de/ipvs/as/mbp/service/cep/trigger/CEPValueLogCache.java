package de.ipvs.as.mbp.service.cep.trigger;

import de.ipvs.as.mbp.domain.valueLog.ValueLog;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches ValueLogs as long as they were not yet written to the database but are needed for the Complex
 * Event Processing. As soon as the ValueLog was written to the database the respective cache entry gets
 * deleted.
 */
@Component
public class CEPValueLogCache {

    private final Map<String, Map<Long, ValueLog>> cache;

    public CEPValueLogCache() {
        cache = new HashMap<>();
    }

    /**
     * Adds a ValueLog to the cache.
     *
     * @param v The valuelog to add to the cache.
     */
    public void addValueLog(ValueLog v) {
        Map<Long, ValueLog> timeStampValueLogMap;

        // Check if Map for component already exists
        if (cache.containsKey(v.getIdref())) {
            timeStampValueLogMap = cache.get(v.getIdref());
            timeStampValueLogMap.put(v.getTime().toEpochMilli(), v);
        } else {
            timeStampValueLogMap = new HashMap<>();
            timeStampValueLogMap.put(v.getTime().toEpochMilli(), v);
            this.cache.put(v.getIdref(), timeStampValueLogMap);
        }

    }

    /**
     * Removes a {@link ValueLog} from the cache. The retrieval of the ValueLog to be removed depends
     * on the componentID and the timestamp of a ValueLog.
     *
     * @param v The ValueLog instance to remove.
     */
    public void removeValueLog(ValueLog v) {
        if (this.cache.containsKey(v.getIdref()) && this.cache.get(v.getIdref()).containsKey(v.getTime().toEpochMilli())) {
            this.cache.remove(v.getIdref()).get(v.getTime().toEpochMilli());
        }

        if (this.cache.containsKey(v.getIdref()) && this.cache.get(v.getIdref()).size() < 1) {
            this.cache.remove(v.getIdref());
        }

    }

    /**
     * Returns a cached {@link ValueLog} by its componentId and timestamp.
     *
     * @param idRef     The component id to which the ValueLog belongs.
     * @param timeStamp The timestamp of the ValueLog (when it was received by the MBP).
     * @return The ValueLog matching the passed arguments.
     */
    public ValueLog getValueLog(String idRef, Instant timeStamp) {
        long time = timeStamp.toEpochMilli();
        if (this.cache.containsKey(idRef)) {
            return this.cache.get(idRef).get(time);
        } else {
            return null;
        }
    }


}
