package de.ipvs.as.mbp.error;

import java.util.HashMap;
import java.util.Map;

public class EntityValidationException extends RuntimeException {

    private Map<String, String> invalidFields = new HashMap<>();

    public EntityValidationException() {
        super();
    }

    public EntityValidationException(String message) {
        super(message);
    }

    public EntityValidationException(Map<String, String> invalidFields) {
        super();
        this.invalidFields = invalidFields;
    }

    public EntityValidationException(String message, Map<String, String> invalidFields) {
        super(message);
        this.invalidFields = invalidFields;
    }

    public Map<String, String> getInvalidFields() {
        return invalidFields;
    }


    public EntityValidationException addInvalidField(String field, String reason) {
        invalidFields.put(field, reason);
        return this;
    }

    public boolean hasInvalidFields() {
        return !invalidFields.isEmpty();
    }

}
