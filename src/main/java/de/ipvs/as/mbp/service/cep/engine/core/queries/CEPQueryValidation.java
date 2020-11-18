package de.ipvs.as.mbp.service.cep.engine.core.queries;

/**
 * Objects of this class represent the output of query validation processes. They consist out of the checked query,
 * a boolean holding the result and an optional error message that describes the problem with the query.
 */
public class CEPQueryValidation {
    private String query;
    private boolean isValid;
    private String errorMessage;

    /**
     * Creates a new validation object by passing a query and its validation result.
     *
     * @param query   The validated query
     * @param isValid The result of the validation; true, if the query is valid; false otherwise
     */
    public CEPQueryValidation(String query, boolean isValid) {
        this(query, isValid, null);
    }

    /**
     * Creates a new validation object by passing a query, its validation result and an error message.
     *
     * @param query        The validated query
     * @param isValid      The result of the validation; true, if the query is valid; false otherwise
     * @param errorMessage An error message describing the problem with the query
     */
    public CEPQueryValidation(String query, boolean isValid, String errorMessage) {
        setQuery(query);
        setValid(isValid);
        setErrorMessage(errorMessage);
    }

    /**
     * Returns the query that was validated.
     *
     * @return The query
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the query that was validated.
     *
     * @param query The query to set
     */
    public void setQuery(String query) {
        //Sanity check
        if ((query == null) || query.isEmpty()) {
            throw new IllegalArgumentException("Query string must not be null or empty.");
        }
        this.query = query;
    }

    /**
     * Returns whether the query is valid.
     *
     * @return True, if the query is valid; false otherwise
     */
    public boolean isValid() {
        return isValid;
    }

    /**
     * Sets whether the query is valid.
     *
     * @param valid rue, if the query is valid; false otherwise
     */
    public void setValid(boolean valid) {
        isValid = valid;
    }

    /**
     * Returns the error message describing the problem with the query, if available.
     *
     * @return The error message. Null, if no error message is available
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the error message describing the problem with the query, if available.
     *
     * @param errorMessage The query message to set. Needs to be set to null, if not available
     */
    public void setErrorMessage(String errorMessage) {
        //Sanity check
        if ((query != null) && query.isEmpty()) {
            throw new IllegalArgumentException("Error message must not be empty.");
        }
        this.errorMessage = errorMessage;
    }

    /**
     * Returns whether an error message is available.
     *
     * @return True, if an error message is available; false otherwise
     */
    public boolean hasErrorMessage() {
        return this.errorMessage != null;
    }
}
