package org.citopt.connde.service.cep.core;

import com.espertech.esper.client.*;
import org.citopt.connde.service.cep.core.events.CEPPrimitiveDataTypes;
import org.citopt.connde.service.cep.core.events.CEPEvent;
import org.citopt.connde.service.cep.core.events.CEPEventType;
import org.citopt.connde.service.cep.core.queries.CEPQuery;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * This component works as a wrapper for the CEP engine Esper (http://esper.espertech.com) and
 * provides basic functionality for working with this engine independently from the MBP.
 */
@Component
public class CEPEngine {
    //Internal fields
    private EPServiceProvider cepService;
    private EPAdministrator cepAdmin;
    private EPRuntime cepRuntime;

    //Stores the event types that have been registered at the engine
    private Set<CEPEventType> registeredEventTypes;

    /**
     * Creates the component by initializing Esper and the corresponding internal fields.
     */
    private CEPEngine() {
        //Get and initialize CEP service
        cepService = EPServiceProviderManager.getDefaultProvider();
        cepService.initialize();

        //Get admin and runtime objects
        cepAdmin = cepService.getEPAdministrator();
        cepRuntime = cepService.getEPRuntime();

        //Create empty set of registered event types
        registeredEventTypes = new HashSet<>();
    }

    /**
     * Creates and registers a new CEP query from a given name and a query string in EPL (Event Processing Language).
     * The registered query is then returned as a CEPQuery object which provides further functionality, such as
     * means for subscription.
     *
     * @param name        The name of the query to create
     * @param queryString The query string of the query described in Esper's Event Processing Language
     * @return The CEPQuery object representing the query
     */
    public CEPQuery createQuery(String name, String queryString) {
        //Sanity checks
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        } else if ((queryString == null) || (queryString.isEmpty())) {
            throw new IllegalArgumentException("Query string must not be null or empty.");
        }

        //Check for duplicate query names
        List<String> statementNames = getAllQueryNames();
        if (statementNames.contains(name)) {
            throw new IllegalArgumentException("A query with this name is already registered.");
        }

        //Create statement with name and query string
        EPStatement statement = cepAdmin.createEPL(queryString, name);

        //Create query object from statement and return
        return new CEPQuery(statement);
    }

    /**
     * Returns a CEPQuery object for a certain query given by its name.
     *
     * @param name The name of the query for which the CEPQuery object is supposed to be returne
     * @return A dedicated CEPQuery object representing the query
     */
    public CEPQuery getQueryByName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        }

        //Retrieve statement by name
        EPStatement statement = cepAdmin.getStatement(name);

        //Sanity check
        if (statement == null) {
            throw new IllegalArgumentException("A query with name \"" + name + "\" is not registered.");
        }

        //Create query object from statement and return
        return new CEPQuery(statement);
    }

    /**
     * Registers a new event type to the CEP engine. After that, events of the new event type might be
     * sent to the engine or used in queries.
     *
     * @param eventType The event type to register
     */
    public void registerEventType(CEPEventType eventType) {
        //Sanity checks
        if (eventType == null) {
            throw new IllegalArgumentException("Event type object must not be null.");
        } else if (registeredEventTypes.contains(eventType)) {
            throw new IllegalArgumentException("This event type has already been registered.");
        }

        /*
        Build up a query string containing the event name and its typed fields for registering
        the event type at the Esper engine.
         */

        //Get fields of event object
        Map<String, CEPPrimitiveDataTypes> eventFields = eventType.getFields();

        //Build query
        StringBuilder queryBuilder = new StringBuilder("Create schema");
        queryBuilder.append(" ");
        queryBuilder.append(eventType.getName());
        queryBuilder.append("(");

        //Iterate over all event fields
        Iterator<String> fieldNameIterator = eventFields.keySet().iterator();
        while (fieldNameIterator.hasNext()) {
            //Get current field name and type
            String fieldName = fieldNameIterator.next();
            CEPPrimitiveDataTypes fieldType = eventFields.get(fieldName);

            //Append field name and its type to the query
            queryBuilder.append(fieldName);
            queryBuilder.append(" ");
            queryBuilder.append(fieldType.getName());

            //Only add a comma if there are further fields to process
            if (fieldNameIterator.hasNext()) {
                queryBuilder.append(", ");
            }
        }

        //Close query
        queryBuilder.append(")");

        //Create statement for query
        cepAdmin.createEPL(queryBuilder.toString());

        //Add event type to set of registered types
        registeredEventTypes.add(eventType);
    }

    /**
     * Sends a given event to the CEP engine such that it can be processed by Esper. The type of the event
     * that is supposed to be sent to the engine needs to be registered before.
     *
     * @param event The event to send
     */
    public void sendEvent(CEPEvent event) {
        //Iterate over all registered event types and Try to find matching event type
        CEPEventType matchingEventType = null;
        for (CEPEventType eventType : registeredEventTypes) {
            //Check if current event type matches the given event
            if (eventType.isValidInstance(event)) {
                matchingEventType = eventType;
                break;
            }
        }

        //Check if event type could be found
        if (matchingEventType == null) {
            throw new IllegalArgumentException("No event type has been registered for this event.");
        }

        //Send valid event to Esper
        cepRuntime.sendEvent(event.getFieldValues(), event.getEventTypeName());
    }

    /**
     * Returns a list of all names of queries that are currently registered at the engine.
     *
     * @return The list of names
     */
    public List<String> getAllQueryNames() {
        //Get names
        String[] names = cepAdmin.getStatementNames();

        //Convert array to list
        return Arrays.asList(names);
    }
}
