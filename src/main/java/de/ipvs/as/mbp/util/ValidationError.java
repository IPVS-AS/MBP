package de.ipvs.as.mbp.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.FieldError;

/**
 * A validation error represents a serializable object originating from a FieldError object that was created
 * as part of an Errors collection during a entity validation.
 */
@ApiModel(description = "Validation error describing the invalidity of an entity property")
public class ValidationError {
    @ApiModelProperty(notes = "Name of the affected entity", example = "Device")
    private String entity;
    @ApiModelProperty(notes = "Name of the affected property of the entity", example = "name")
    private String property;
    @ApiModelProperty(notes = "Message describing why the entity property is invalid", example = "An entity with this name already exists")
    private String message;

    /**
     * Creates a new validation error from a field error
     *
     * @param fieldError The field error to create a validation error from
     */
    ValidationError(FieldError fieldError) {
        //Sanity check
        if (fieldError == null) {
            throw new IllegalArgumentException("Field error  must not be null.");
        }

        //Set fields
        this.entity = fieldError.getObjectName();
        this.property = fieldError.getField();
        this.message = fieldError.getDefaultMessage();
    }

    /**
     * Returns the entity name of the validation error.
     *
     * @return The entity name
     */
    public String getEntity() {
        return entity;
    }

    /**
     * Sets the entity name of the validation error.
     *
     * @param entity The entity name to set
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }

    /**
     * Returns the property name of the validation error.
     *
     * @return The property name
     */
    public String getProperty() {
        return property;
    }

    /**
     * Sets the property name of the validation error.
     *
     * @param property The property name to set
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * Returns the message of the validation error.
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets the message of the validation error
     *
     * @param message The message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
}
