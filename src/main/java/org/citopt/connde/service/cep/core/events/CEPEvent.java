package org.citopt.connde.service.cep.core.events;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Objects of subclasses of this class represent CEP events that might be sent to the CEP engine for the purpose
 * of event processing. Each single event type implementation needs to extend this class. An event object
 * can be considered as an instance of a certain event type object and needs to provide at least the fields that
 * are defined in the dedicated event type object of the same name.
 */
public abstract class CEPEvent {
    //Map (field name --> field value) of field values that are part of the event
    private Map<String, Object> fieldValues;

    /**
     * Creates a new CEP event.
     */
    public CEPEvent() {
        this.fieldValues = new HashMap<>();
    }

    /**
     * Sets a field of this event to a certain value.
     *
     * @param fieldName  The name of the field to set
     * @param fieldValue The value to set
     */
    public void addValue(String fieldName, Object fieldValue) {
        //Sanity checks
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        } else if (fieldValues.containsKey(fieldName)) {
            throw new IllegalArgumentException("A value for a field of this name has been added already.");
        }

        //Add field name and value to the values map
        fieldValues.put(fieldName, fieldValue);
    }

    /**
     * Returns a map (field name --> field value) of field values that have been added to this object.
     *
     * @return The mao of field values
     */
    public Map<String, Object> getFieldValues() {
        return fieldValues;
    }

    /**
     * Returns the name of the event type to which this event object refers to. Generally,
     * event objects can be considered as instances of event type objects. The link between event objects
     * and event type objects is created by the name that is returned by this method.
     *
     * @return The event type name
     */
    public abstract String getEventTypeName();
}
