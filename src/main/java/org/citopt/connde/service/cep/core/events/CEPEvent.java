package org.citopt.connde.service.cep.core.events;

import java.util.HashMap;
import java.util.Map;

public abstract class CEPEvent {
    private Map<String, Object> fieldValues;

    public CEPEvent() {
        this.fieldValues = new HashMap<>();
    }

    public abstract String getEventTypeName();

    public void addValue(String fieldName, Object fieldValue) {
        //Sanity check
        if ((fieldName == null) || (fieldName.isEmpty())) {
            throw new IllegalArgumentException("Field name must not be null or empty.");
        } else if (fieldValues.containsKey(fieldName)) {
            throw new IllegalArgumentException("A value for a field of this name has been added already.");
        }

        fieldValues.put(fieldName, fieldValue);
    }

    public Map<String, Object> getFieldValues() {
        return fieldValues;
    }
}
