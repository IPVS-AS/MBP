package org.citopt.connde.service.cep.engine.esper;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementState;
import org.citopt.connde.service.cep.engine.core.queries.CEPQuery;
import org.citopt.connde.service.cep.engine.core.queries.CEPQuerySubscriber;

/**
 * Objects of this class wrap Esper CEP queries that were created by the Esper CEP engine.
 */
public class EsperCEPQuery implements CEPQuery {
    //The dedicated query statement created by the engine
    private EPStatement statement;

    /**
     * Creates a new query object by passing a dedicated statement created by the CEP engine.
     *
     * @param statement The statement of the query to wrap
     */
    EsperCEPQuery(EPStatement statement) {
        setStatement(statement);
    }

    /**
     * Sets the subscriber of the query which needs to implement the CEPQuerySubscriber interface.
     * Only one subscriber may be registered at the query; multiple calls of this method with different
     * subscribers make the subscriber override each other.
     *
     * @param subscriber The subscriber to set
     */
    public void setSubscriber(CEPQuerySubscriber subscriber) {
        //Sanity check
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber must not be null.");
        }

        //Create subscriber dispatcher that will notify the subscriber on callback
        EsperCEPQueryDispatcher dispatcher = new EsperCEPQueryDispatcher(subscriber);

        //Set subscriber
        statement.setSubscriber(dispatcher);
    }

    /**
     * Unregisters the query from the CEP engine.
     */
    public void unregister() {
        statement.destroy();
    }

    /**
     * Enables the query at the CEP engine again in case it has been disabled before.
     */
    public void enable() {
        statement.start();
    }

    /**
     * Disables the query at the CEP engine.
     */
    public void disable() {
        statement.stop();
    }

    /**
     * Checks whether the query is currently active.
     *
     * @return True, if the query is currently active; false otherwise
     */
    public boolean isActive() {
        return statement.isStarted();
    }

    /**
     * Returns the current state of the query.
     *
     * @return The state
     */
    public EPStatementState getState() {
        return statement.getState();
    }

    /**
     * Returns the query statement of this query object.
     *
     * @return The statement
     */
    protected EPStatement getStatement() {
        return statement;
    }

    /**
     * Sets the query statement of this query object.
     *
     * @param statement The statement to set
     */
    protected void setStatement(EPStatement statement) {
        //Sanity check
        if (statement == null) {
            throw new IllegalArgumentException("Statement must not be null.");
        }
        this.statement = statement;
    }
}
