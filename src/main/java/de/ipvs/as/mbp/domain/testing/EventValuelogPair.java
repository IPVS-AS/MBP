package de.ipvs.as.mbp.domain.testing;

import de.ipvs.as.mbp.domain.valueLog.ValueLog;

/**
 * Class for storing a event name key (e.g. "event_0") with its corresponding {@link ValueLog}.
 */
public class EventValuelogPair {

    private String eventName;
    private ValueLog valueLog;

    public EventValuelogPair(String eventName, ValueLog valueLog) {
        this.eventName = eventName;
        this.valueLog = valueLog;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public ValueLog getValueLog() {
        return valueLog;
    }

    public void setValueLog(ValueLog valueLog) {
        this.valueLog = valueLog;
    }
}
