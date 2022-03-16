package de.ipvs.as.mbp.service.cep.engine.core.queries;

import com.espertech.esper.runtime.client.EPDeployException;
import com.espertech.esper.runtime.client.EPUndeployException;

/**
 * Interface for implementations of engine-specific CEP query objects.
 */
public interface CEPQuery {
    /**
     * Sets the subscriber of the query which needs to implement the CEPQuerySubscriber interface.
     * Only one subscriber may be registered at the query; multiple calls of this method with different
     * subscribers make the subscriber override each other.
     *
     * @param subscriber The subscriber to set
     */
    void setSubscriber(CEPQuerySubscriber subscriber);

    /**
     * Unregisters the query from the CEP engine.
     */
    void unregister() throws EPUndeployException;

    /**
     * Enables the query at the CEP engine again in case it has been disabled before.
     */
    void enable() throws EPDeployException;

    /**
     * Disables the query at the CEP engine.
     */
    void disable() throws EPUndeployException;

    /**
     * Checks whether the query is currently active.
     *
     * @return True, if the query is currently active; false otherwise
     */
    boolean isActive();
}
