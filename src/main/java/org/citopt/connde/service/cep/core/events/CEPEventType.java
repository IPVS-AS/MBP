package org.citopt.connde.service.cep.core.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CEPEventType {
    private String name;
    private Map<String, CEPDataType> fields;

    public CEPEventType(String name) {
        this.name = name;
        this.fields = new HashMap<>();
    }

    public void addField(String fieldName, CEPDataType dataType) {
        //Sanity check
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        } else if (fields.containsKey(fieldName)) {
            throw new IllegalArgumentException("A field with this name is already part of the event type.");
        } else if (dataType == null) {
            throw new IllegalArgumentException("Data type must not be null.");
        }

        fields.put(fieldName, dataType);
    }

    public String getName() {
        return name;
    }

    public Map<String, CEPDataType> getFields() {
        return fields;
    }

    public boolean isValidInstance(CEPEvent cepEvent) {
        //Sanity check
        if (cepEvent == null) {
            throw new IllegalArgumentException("Event object must not be null.");
        }

        //Compare event names
        if (!cepEvent.getEventTypeName().equals(name)) {
            return false;
        }

        //Get field values of the provided event
        Map<String, Object> fieldValues = cepEvent.getFieldValues();

        //Iterate over all defined fields of this event type
        Set<String> fieldNames = fields.keySet();
        for (String fieldName : fieldNames) {
            if (!fieldValues.containsKey(fieldName)) {
                return false;
            }

            Object fieldValue = fieldValues.get(fieldName);
            Class referenceClass = fields.get(fieldName).getReferenceClass();

            if (!referenceClass.isInstance(fieldValue)) {
                return false;
            }
        }

        //in all other cases, the event is a valid instance
        return true;
    }
}
