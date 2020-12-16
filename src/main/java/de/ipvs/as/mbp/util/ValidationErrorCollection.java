package de.ipvs.as.mbp.util;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;

import java.util.LinkedList;
import java.util.List;

/**
 * Objects of this class represent serializable collections of errors that occurred during the validation of an entity,
 * originally wrapped within Errors objects.
 */
@ApiModel(description = "Collection of validation errors")
public class ValidationErrorCollection {
    @ApiModelProperty(notes = "List of validation errors")
    private List<ValidationError> errors = new LinkedList<>();

    /**
     * Creates a new validation errors collection from an Errors object that was created during the validation
     * of an entity.
     *
     * @param errors The Errors object to create the collection from
     */
    public ValidationErrorCollection(Errors errors) {
        //Iterate over all available field errors
        for (FieldError fieldError : errors.getFieldErrors()) {
            //Create validation error object from field error
            ValidationError validationError = new ValidationError(fieldError);

            //Add validation error to list
            this.errors.add(validationError);
        }
    }

    /**
     * Returns the list of validation errors that are part of the collection.
     *
     * @return The list of validation errors
     */
    public List<ValidationError> getErrors() {
        return errors;
    }

    /**
     * Sets the list of validation errors that are supposed to be part of the collection.
     *
     * @param errors The list of validation errors to set
     */
    public void setErrors(List<ValidationError> errors) {
        //Sanity check
        if (errors == null) {
            throw new IllegalArgumentException("List of validation errors must be null.");
        }

        this.errors = errors;
    }
}
