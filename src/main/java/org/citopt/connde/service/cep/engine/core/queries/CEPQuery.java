package org.citopt.connde.service.cep.engine.core.queries;

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
    void unregister();

    /**
     * Enables the query at the CEP engine again in case it has been disabled before.
     */
    void enable();

    /**
     * Disables the query at the CEP engine.
     */
    void disable();

    /**
     * Checks whether the query is currently active.
     *
     * @return True, if the query is currently active; false otherwise
     */
    boolean isActive();
}
