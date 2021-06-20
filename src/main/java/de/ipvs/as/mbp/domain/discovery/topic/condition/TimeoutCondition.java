package de.ipvs.as.mbp.domain.discovery.topic.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.error.EntityValidationException;

/**
 * Completeness condition based on timeout.
 */
@JsonIgnoreProperties
public class TimeoutCondition extends CompletenessCondition {
    //Type name of the condition
    private static final String TYPE_NAME = "counter";

    //Timeout to wait in milliseconds
    private int timeout;

    /**
     * Creates a new timeout-based completeness condition.
     */
    public TimeoutCondition() {
        super();
    }

    /**
     * Creates a new timeout-based completeness condition from a given timeout value.
     *
     * @param timeout The timeout value to use
     */
    @JsonCreator
    public TimeoutCondition(@JsonProperty("timeout") int timeout) {
        setTimeout(timeout);
    }

    /**
     * Returns the timeout of the condition.
     *
     * @return The timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Sets the timeout of the condition
     *
     * @param timeout The timeout to set in millisecons
     * @return The condition
     */
    public TimeoutCondition setTimeout(int timeout) {
        //Sanity check
        if (timeout < 10) {
            throw new IllegalArgumentException("Timeout must not be smaller than 10 milliseconds.");
        }

        //Set timeout
        this.timeout = timeout;
        return this;
    }

    /**
     * Returns the name of the completeness condition.
     *
     * @return The name
     */
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Returns a human-readable description of the completeness condition.
     *
     * @return The description
     */
    @Override
    public String getDescription() {
        return String.format("Wait %d milliseconds.", this.timeout);
    }

    /**
     * Validates the completeness condition by extending the provided exception with information about invalid fields.
     *
     * @param exception The exception to extend as part of the validation
     */
    @Override
    public void validate(EntityValidationException exception) {
        //Check timeout
        if (this.timeout < 10) {
            exception.addInvalidField("timeout", "The timeout must not be smaller than 10 milliseconds.");
        }
    }
}
