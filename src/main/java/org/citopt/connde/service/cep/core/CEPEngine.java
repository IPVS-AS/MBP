package org.citopt.connde.service.cep.core;

import com.espertech.esper.client.*;
import org.citopt.connde.service.cep.core.events.CEPDataType;
import org.citopt.connde.service.cep.core.events.CEPEvent;
import org.citopt.connde.service.cep.core.events.CEPEventType;
import org.citopt.connde.service.cep.core.queries.CEPQuery;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CEPEngine {

    private EPServiceProvider cepService;
    private EPAdministrator cepAdmin;
    private EPRuntime cepRuntime;

    private Set<CEPEventType> registeredEventTypes;

    private CEPEngine() {
        cepService = EPServiceProviderManager.getDefaultProvider();
        cepService.initialize();

        cepAdmin = cepService.getEPAdministrator();
        cepRuntime = cepService.getEPRuntime();

        registeredEventTypes = new HashSet<>();
    }

    public CEPQuery createQuery(String name, String queryString) {
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        } else if ((queryString == null) || (queryString.isEmpty())) {
            throw new IllegalArgumentException("Query string must not be null or empty.");
        }

        List<String> statementNames = getAllQueryNames();
        if (statementNames.contains(name)) {
            throw new IllegalArgumentException("A query with this name is already registered.");
        }

        EPStatement statement = cepAdmin.createEPL(queryString, name);
        return new CEPQuery(statement);
    }

    public CEPQuery getQueryByName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        }

        //Retrieve statement with given name
        EPStatement statement = cepAdmin.getStatement(name);

        //Sanity check
        if (statement == null) {
            throw new IllegalArgumentException("A query with name \"" + name + "\" is not registered.");
        }

        return new CEPQuery(statement);
    }

    public void registerEventType(CEPEventType eventType) {
        if (eventType == null) {
            throw new IllegalArgumentException("Event type object must not be null.");
        } else if (registeredEventTypes.contains(eventType)) {
            throw new IllegalArgumentException("This event type has already been registered.");
        }

        //Get fields of event object
        Map<String, CEPDataType> eventFields = eventType.getFields();

        //Build query
        StringBuilder queryBuilder = new StringBuilder("Create schema");
        queryBuilder.append(" ");
        queryBuilder.append(eventType.getName());
        queryBuilder.append("(");

        //Iterate over all event fields
        Set<String> fieldNames = eventFields.keySet();
        Iterator<String> fieldNameIterator = fieldNames.iterator();
        while (fieldNameIterator.hasNext()) {
            String fieldName = fieldNameIterator.next();
            CEPDataType fieldType = eventFields.get(fieldName);

            queryBuilder.append(fieldName);
            queryBuilder.append(" ");
            queryBuilder.append(fieldType.getTypeName());

            if (fieldNameIterator.hasNext()) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(")");

        //Create statement for query
        cepAdmin.createEPL(queryBuilder.toString());

        //Add event type to set of registered types
        registeredEventTypes.add(eventType);
    }

    public void sendEvent(CEPEvent event) {
        //Try to find matching event type in set of registered events
        CEPEventType matchingEventType = null;
        for (CEPEventType eventType : registeredEventTypes) {
            if (eventType.isValidInstance(event)) {
                matchingEventType = eventType;
                break;
            }
        }

        //Check if event type could be found
        if (matchingEventType == null) {
            throw new IllegalArgumentException("No event type has been registered for this event.");
        }

        //Send event since it is valid
        cepRuntime.sendEvent(event.getFieldValues(), event.getEventTypeName());
    }

    public List<String> getAllQueryNames() {
        String[] names = cepAdmin.getStatementNames();
        return Arrays.asList(names);
    }
}
