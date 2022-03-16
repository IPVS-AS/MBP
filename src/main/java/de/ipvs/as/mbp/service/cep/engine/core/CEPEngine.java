package de.ipvs.as.mbp.service.cep.engine.core;

import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.runtime.client.EPUndeployException;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEvent;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEventType;
import de.ipvs.as.mbp.service.cep.engine.core.exceptions.EventNotRegisteredException;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQuery;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQueryValidation;

import java.util.List;

/**
 * Interface for implementations of Complex Event Processing (CEP) engines.
 */
public interface CEPEngine {

    /**
     * Creates and registers a new CEP query from a given name and a query string. The registered query is then
     * returned as a CEPQuery object which provides further functionality, such as means for subscription.
     *
     * @param name        The name of the query to create
     * @param queryString The query string of the query
     * @return The CEPQuery object representing the query
     */
    CEPQuery createQuery(String name, String queryString) throws EPCompileException;

    /**
     * Returns a CEPQuery object for a certain query given by its name.
     *
     * @param name The name of the query for which the CEPQuery object is supposed to be returned
     * @return A dedicated CEPQuery object representing the query
     */
    CEPQuery getQueryByName(String name) throws EPCompileException;

    /**
     * Registers a new event type at the CEP engine. After that, events of the new event type might be
     * sent to the engine or used in queries.
     *
     * @param eventType The event type to register
     */
    void registerEventType(CEPEventType eventType);

    /**
     * Sends a given event to the CEP engine so that it can be processed. The type of the event
     * that is supposed to be sent to the engine needs to be registered before.
     *
     * @param event The event to publish
     */
    void sendEvent(CEPEvent event) throws EventNotRegisteredException;

    /**
     * Validates a given query string and checks whether it is syntactically and semantically valid.
     *
     * @param queryString The query string to check
     * @return The result of the validation wrapped in a validation object
     */
    CEPQueryValidation validateQuery(String queryString);

    /**
     * Returns a list of all names of queries that are currently registered at the engine.
     *
     * @return The list of names
     */
    List<String> getAllQueryNames();
}
