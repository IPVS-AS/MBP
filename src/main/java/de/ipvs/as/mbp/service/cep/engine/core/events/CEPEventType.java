package de.ipvs.as.mbp.service.cep.engine.core.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Objects of this class represent definitions of event types that might be registered at the CEP engine.
 * A event type definition consists out of a name and a map of typed fields that need to be available
 * in event instances.
 */
public class CEPEventType {
    //Name of the event type and its instances
    private String name;
    //Map (field name --> data type) of fields that are part of instances of this event type
    private Map<String, CEPPrimitiveDataTypes> fields;

    /**
     * Creates a new event type object with a given name.
     *
     * @param name The name of the event type
     */
    public CEPEventType(String name) {
        //Set name
        setName(name);

        //Set empty map of fields
        this.fields = new HashMap<>();
    }

    /**
     * Adds a field to the event type, given by a desired name and data type. The field name
     * must be unique for an event type.
     *
     * @param fieldName The name of the field
     * @param dataType  The data type of which values of the field should be
     */
    public void addField(String fieldName, CEPPrimitiveDataTypes dataType) {
        //Sanity checks
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        } else if (fields.containsKey(fieldName)) {
            throw new IllegalArgumentException("A field with this name is already part of the event type.");
        } else if (dataType == null) {
            throw new IllegalArgumentException("Data type must not be null.");
        }

        //Add name and data type to field map
        fields.put(fieldName, dataType);
    }

    /**
     * Checks whether a given event object is a valid instance of this event type. In order to be
     * a valid instance of this event type, the event object needs to have the same name and to provide
     * type-compatible values for at least the same fields that are defined in the event type. However,
     * it is allowed to add more fields to the event object than defined in its corresponding event type.
     *
     * @param event The event object to check
     * @return True, if the event object is a valid instance of this event type; false otherwise
     */
    public boolean isValidInstance(CEPEvent event) {
        //Sanity check
        if (event == null) {
            throw new IllegalArgumentException("Event object must not be null.");
        }

        //Event names must be equal
        if (!event.getEventTypeName().equals(name)) {
            return false;
        }

        //Get field values of the provided event
        Map<String, Object> fieldValues = event.getFieldValues();

        //Iterate over all defined fields of this event type
        Set<String> fieldNames = fields.keySet();
        for (String fieldName : fieldNames) {
            //Event object must contain at least fields of the same name as those defined in the event type
            if (!fieldValues.containsKey(fieldName)) {
                return false;
            }

            //Get value of the field
            Object fieldValue = fieldValues.get(fieldName);

            //Get reference class of the field as defined in the corresponding event type
            Class<?> referenceClass = fields.get(fieldName).getReferenceClass();

            //The value that is set in the event object must match the type as defined in the event type
            if (!referenceClass.isInstance(fieldValue)) {
                return false;
            }
        }

        //in all other cases, the event is a valid instance
        return true;
    }

    /**
     * Returns the name of the event type.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the event type.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        }
        this.name = name;
    }

    /**
     * Returns a map (field name --> data type) of fields that need to be part of instances of this event type.
     *
     * @return The map of fields
     */
    public Map<String, CEPPrimitiveDataTypes> getFields() {
        return fields;
    }
}
