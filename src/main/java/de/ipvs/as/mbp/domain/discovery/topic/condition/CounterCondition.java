package de.ipvs.as.mbp.domain.discovery.topic.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.error.EntityValidationException;

/**
 * Completeness condition based on the number of received replies.
 */
@JsonIgnoreProperties
public class CounterCondition extends CompletenessCondition {
    //Type name of the condition
    private static final String TYPE_NAME = "counter";

    //Number of replies to receive
    private int replies;

    /**
     * Creates a new counter-based completeness condition.
     */
    public CounterCondition() {
        super();
    }

    /**
     * Creates a new counter-based completeness condition from a given replies value.
     *
     * @param replies The expected number of replies to use
     */
    @JsonCreator
    public CounterCondition(@JsonProperty("replies") int replies) {
        setReplies(replies);
    }

    /**
     * Returns the number of required replies.
     *
     * @return The number of replies
     */
    public int getReplies() {
        return replies;
    }

    /**
     * Sets the number of required replies.
     *
     * @param replies The number of replies to set
     * @return The condition
     */
    public CounterCondition setReplies(int replies) {
        //Sanity check
        if (replies < 1) {
            throw new IllegalArgumentException("Number of required replies must be greater than zero.");
        }

        //Set the number of replies
        this.replies = replies;
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
        return String.format("Wait for %d replies.", this.replies);
    }

    /**
     * Validates the completeness condition by extending the provided exception with information about invalid fields.
     *
     * @param exception The exception to extend as part of the validation
     */
    @Override
    public void validate(EntityValidationException exception) {
        //Check number of replies
        if (this.replies < 1) {
            exception.addInvalidField("replies", "The number of replies must be greater than zero.");
        }
    }
}
