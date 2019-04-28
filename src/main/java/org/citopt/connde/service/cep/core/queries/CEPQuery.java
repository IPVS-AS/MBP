package org.citopt.connde.service.cep.core.queries;

import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EPStatementState;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

public class CEPQuery {
    private EPStatement statement;

    public CEPQuery(EPStatement statement) {
        setStatement(statement);
    }

    public void setSubscriber(CEPQuerySubscriber subscriber) {
        //Sanity check
        if (subscriber == null) {
            throw new IllegalArgumentException("Subscriber must not be null.");
        }

        //Create subscriber dispatcher that calls the provided subscriber
        CEPQueryCallbackDispatcher dispatcher = new CEPQueryCallbackDispatcher(subscriber);

        //Set subscriber
        statement.setSubscriber(dispatcher);
    }

    public void unregister() {
        statement.destroy();
    }

    public void enable() {
        statement.start();
    }

    public void disable() {
        statement.stop();
    }

    public boolean isActive() {
        return statement.isStarted();
    }

    public EPStatementState getState() {
        return statement.getState();
    }

    protected EPStatement getStatement() {
        return statement;
    }

    protected void setStatement(EPStatement statement) {
        //Sanity check
        if (statement == null) {
            throw new IllegalArgumentException("Statement must not be null.");
        }
        this.statement = statement;
    }
}
