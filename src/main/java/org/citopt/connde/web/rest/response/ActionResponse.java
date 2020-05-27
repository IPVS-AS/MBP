package org.citopt.connde.web.rest.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Objects of this class represent server responses for action requests (e.g. deployment of sensors).
 */
@ApiModel(description = "Response to requests for executing a certain actiion")
public class ActionResponse {
    //Indicates whether the action was performed successfully
    @ApiModelProperty(notes = "Indicates whether the requested action was executed successfully", example = "true")
    private boolean success;
    //Message that contains global information about the request
    @ApiModelProperty(notes = "A message containing information about the whole request", example = "Action successful")
    private String globalMessage;
    //(Field --> error message) Messages that contain specific information about individual involved components
    @ApiModelProperty(notes = "A mapping (component -> error message) containing specific information about individual involved components", example = "{\"name\": \"Illegal name provided\", \"password\": \"Wrong password\"}")
    private Map<String, String> fieldErrors;

    /**
     * Creates a new response object for a certain action that was request before.
     *
     * @param success Indicates whether the action was exectued successfully
     */
    public ActionResponse(boolean success) {
        this.success = success;
        this.globalMessage = "";
        this.fieldErrors = new HashMap<>();
    }

    /**
     * Creates a new response object for a certain action that was request before.
     *
     * @param success       Indicates whether the action was executed successfully
     * @param globalMessage A global message that describes the result of the action
     */
    public ActionResponse(boolean success, String globalMessage) {
        this(success);
        this.globalMessage = globalMessage;
    }

    /**
     * Indicates whether the requested action was executed successfully.
     *
     * @return True, if the action was executed successfully; false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Determines whether requested action was executed successfully.
     *
     * @param success True, if the action was executed successfully; false otherwise
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Sets a global message that describes the overall result of the action.
     *
     * @param globalMessage The message to set
     */
    public void setGlobalError(String globalMessage) {
        this.globalMessage = globalMessage;
    }

    /**
     * Returns the global message that describes the overall result of the action.
     *
     * @return The global message
     */
    public String getGlobalMessage() {
        return globalMessage;
    }

    /**
     * Adds a field specific error message to the response object that describes the issue that occurred
     * while executing the action because of this field (e.g. validation issues).
     *
     * @param fieldName The name of the field
     * @param message   The error message for this field
     */
    public void addFieldError(String fieldName, String message) {
        //Sanity check
        if (fieldName == null) {
            throw new IllegalArgumentException("Field name must not be null.");
        }
        this.fieldErrors.put(fieldName, message);
    }

    /**
     * Sets a map (field name --> error message) that contains error messages which describe the issue
     * that occurred because of certain fields while executing the action (e.g. validation issues).
     *
     * @param fieldErrors The map of field errors to set
     */
    public void setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
    }

    /**
     * Returns a map (field name --> error message) that contains error messages which describe the issue
     * that occurred because of certain fields while executing the action (e.g. validation issues).
     *
     * @return The map of field errors
     */
    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
