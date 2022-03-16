package de.ipvs.as.mbp.service.cep.engine.esper;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.runtime.client.*;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQuery;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQuerySubscriber;

/**
 * Objects of this class wrap Esper CEP queries that were created by the Esper CEP engine.
 */
public class EsperCEPQuery implements CEPQuery {
    //The dedicated query statement created by the engine
    private EPStatement statement;

    private final EPCompiled compiledStatement;

    private EPDeployment currDeployment;

    private final EPDeploymentService cepDeploymentService;


    /**
     * Creates a new query object by passing a dedicated statement created by the CEP engine.
     *
     * @param compiledStatement The compiled statement of the query to wrap
     */
    EsperCEPQuery(EPCompiled compiledStatement) {
        this.compiledStatement = compiledStatement;

        this.currDeployment = null;

        cepDeploymentService = EPRuntimeProvider.getDefaultRuntime().getDeploymentService();
    }

    private void updateDeploymentAndStatement(EPDeployment newDeplyoment) {
        if (newDeplyoment == null) {
            this.statement = null;
        } else {
            this.statement = newDeplyoment.getStatements()[0];
        }
        this.currDeployment = newDeplyoment;
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
    public void unregister() throws EPUndeployException {
        cepDeploymentService.undeploy(statement.getDeploymentId());
    }

    /**
     * Enables the query at the CEP engine again in case it has been disabled before.
     */
    public void enable() throws EPDeployException {
        EPDeployment currDepl = cepDeploymentService.deploy(compiledStatement);
        updateDeploymentAndStatement(currDepl);
    }

    /**
     * Disables the query at the CEP engine.
     */
    public void disable() throws EPUndeployException {
        cepDeploymentService.undeploy(statement.getDeploymentId());
    }

    /**
     * Checks whether the query is currently active (=isDestroyed).
     *
     * @return True, if the query is currently active; false otherwise
     */
    public boolean isActive() {
        return statement.isDestroyed();
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

    public EPDeployment getCurrDeployment() {
        return currDeployment;
    }
}
