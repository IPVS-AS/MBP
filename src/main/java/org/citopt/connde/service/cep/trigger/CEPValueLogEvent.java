package org.citopt.connde.service.cep.trigger;

import org.citopt.connde.domain.valueLog.ValueLog;
import org.citopt.connde.service.cep.engine.core.events.CEPEvent;

import java.time.Instant;

/**
 * CEP event wrapping a value log that was received for a certain component. This event may be used in order to be
 * further processed by the CEP engine.
 */
public class CEPValueLogEvent extends CEPEvent {

    //Value log to wrap
    private ValueLog valueLog;

    /**
     * Creates a new CEP value log event from a given value log.
     *
     * @param valueLog The value log to use
     */
    CEPValueLogEvent(ValueLog valueLog) {
        super();

        //Sanity check
        if (valueLog == null) {
            throw new IllegalArgumentException("Value log must not be null.");
        }
        this.valueLog = valueLog;

        //Convert value string of value log to double
        String value = valueLog.getValue();

        //Get Instant object from value log
        Instant time = valueLog.getTime();

        //Get epoch seconds
        long unixSeconds = time.getEpochSecond();

        //Set event fields
        this.addValue("value", value);
        this.addValue("time", unixSeconds);
    }

    /**
     * Returns the name of the event type to which this event object refers to. Generally,
     * event objects can be considered as instances of event type objects. The link between event objects
     * and event type objects is created by the name that is returned by this method.
     *
     * @return The event type name
     */
    @Override
    public String getEventTypeName() {
        return generateEventTypeName(valueLog.getIdref(), valueLog.getComponent());
    }

    /**
     * Returns the value log that is wrapped by the value log event.
     *
     * @return The value log
     */
    public ValueLog getValueLog() {
        return valueLog;
    }

    /**
     * Generates a name for a value log event of a certain component.
     *
     * @param componentId       The id of the component to which the event belongs to
     * @param componentTypeName The type name of the component to which the event belongs to
     * @return The generated event name
     */
    static String generateEventTypeName(String componentId, String componentTypeName) {
        //Normalize component id and type name
        String normalizedTypeName = componentTypeName.toLowerCase();
        String normalizedId = componentId.replace("@", "_").toLowerCase();

        return normalizedTypeName + "_" + normalizedId;
    }
}
