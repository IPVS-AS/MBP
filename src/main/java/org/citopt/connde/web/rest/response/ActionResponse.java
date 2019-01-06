package org.citopt.connde.web.rest.response;

import java.util.HashMap;
import java.util.Map;

public class ActionResponse {
    private boolean success;
    private String globalMessage;
    private Map<String, String> fieldErrors;

    public ActionResponse(boolean success) {
        this.success = success;
        this.globalMessage = "";
        this.fieldErrors = new HashMap<>();
    }

    public ActionResponse(boolean success, String errorMessage) {
        this(success);
        this.globalMessage = errorMessage;
    }

    public void addFieldError(String fieldName, String message) {
        if (fieldName == null) {
            throw new IllegalArgumentException("Path must not be null.");
        }
        this.fieldErrors.put(fieldName, message);
    }

    public void setGlobalError(String errorMessage) {
        this.globalMessage = errorMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getGlobalMessage() {
        return globalMessage;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
