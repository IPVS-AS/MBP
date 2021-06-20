package de.ipvs.as.mbp.domain.discovery.topic.condition;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.error.EntityValidationException;

/**
 * Base class for completeness conditions that specify when the sending of replies to a foregone request can be
 * considered as finished.
 */
public abstract class CompletenessCondition {
    /**
     * Returns the name of the completeness condition.
     *
     * @return The name
     */
    public abstract String getTypeName();

    /**
     * Returns a human-readable description of the completeness condition.
     *
     * @return The description
     */
    @JsonProperty("description")
    public abstract String getDescription();

    /**
     * Validates the completeness condition by extending the provided exception with information about invalid fields.
     *
     * @param exception The exception to extend as part of the validation
     */
    public abstract void validate(EntityValidationException exception);
}
