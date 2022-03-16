package de.ipvs.as.mbp.service.cep.engine.esper;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EPException;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.common.client.module.Module;
import com.espertech.esper.common.client.module.ParseException;
import com.espertech.esper.common.client.soda.EPStatementObjectModel;
import com.espertech.esper.common.client.util.StatementProperty;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.compiler.client.option.StatementNameContext;
import com.espertech.esper.compiler.client.option.StatementNameOption;
import com.espertech.esper.runtime.client.*;
import com.espertech.esper.runtime.client.EPRuntime;
import de.ipvs.as.mbp.service.cep.engine.core.CEPEngine;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEvent;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPEventType;
import de.ipvs.as.mbp.service.cep.engine.core.events.CEPPrimitiveDataTypes;
import de.ipvs.as.mbp.service.cep.engine.core.exceptions.EventNotRegisteredException;
import de.ipvs.as.mbp.service.cep.engine.core.queries.CEPQueryValidation;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * This component implements the interface of CEP engines by making use of the
 * CEP engine Esper (http://esper.espertech.com). It provides basic functionality for working with this engine.
 */
public class EsperCEPEngine implements CEPEngine {
    //Internal fields
    private final EPDeploymentService cepDeploymentService;
    private final EPRuntime cepRuntime;
    private final EPEventService cepEventService;

    //Stores the event types that have been registered at the engine
    private final Set<CEPEventType> registeredEventTypes;

    private final EPCompiler cepCompiler;

    /**
     * Creates the component by initializing Esper and the corresponding internal fields.
     */
    public EsperCEPEngine() {

        //Get and initialize CEP service
        cepRuntime = EPRuntimeProvider.getDefaultRuntime();
        cepRuntime.initialize();

        cepEventService = cepRuntime.getEventService();

        //Get admin and runtime objects
        cepDeploymentService = cepRuntime.getDeploymentService();

        cepCompiler = EPCompilerProvider.getCompiler();


        //Create empty set of registered event types
        registeredEventTypes = new HashSet<>();
    }

    /**
     * Creates and registers a new CEP query from a given name and a query string. The registered query is then
     * returned as a CEPQuery object which provides further functionality, such as means for subscription.
     *
     * @param name        The name of the query to create
     * @param queryString The query string of the query
     * @return The CEPQuery object representing the query
     */
    public EsperCEPQuery createQuery(String name, String queryString) throws EPCompileException {
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
        CompilerArguments args = new CompilerArguments();
        args.getOptions().setStatementName(new StatementNameOption() {
            @Override
            public String getValue(StatementNameContext statementNameContext) {
                return name;
            }
        });
        EPCompiled compiled = cepCompiler.compile(queryString, args);

        //Create query object from statement and return
        return new EsperCEPQuery(compiled);
    }

    /**
     * Returns a CEPQuery object for a certain query given by its name. If no query with such a name
     * is registered, null is returned.
     *
     * @param name The name of the query for which the CEPQuery object is supposed to be returned
     * @return A dedicated CEPQuery object representing the query
     */
    public EsperCEPQuery getQueryByName(String name) throws EPCompileException {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        }

        String[] allDeploymentIds = cepDeploymentService.getDeployments();
        EPStatement statementToFind = null;

        //Retrieve statement by name
        for (String deploymentId : allDeploymentIds) {
            EPDeployment deployment = cepDeploymentService.getDeployment(deploymentId);

            EPStatement[] allStatementsOfCurrDeployment = deployment.getStatements();
            for (EPStatement statement : allStatementsOfCurrDeployment) {
                if (statement.getName().equals(name)) {
                    statementToFind = statement;
                }
            }
        }

        //Sanity check
        if (statementToFind == null) {
            return null;
        }

        //Create query object from statement and return
        return createQuery(statementToFind.getName(), (String) statementToFind.getProperty(StatementProperty.EPL));
    }

    /**
     * Registers a new event type at the CEP engine. After that, events of the new event type might be
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
            queryBuilder.append("`" + fieldName + "`");
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
        //cepDeploymentService.createEPL(queryBuilder.toString());

        //Add event type to set of registered types
        registeredEventTypes.add(eventType);
    }

    /**
     * Sends a given event to the CEP engine so that it can be processed. The type of the event
     * that is supposed to be sent to the engine needs to be registered before.
     *
     * @param event The event to publish
     */
    public void sendEvent(CEPEvent event) throws EventNotRegisteredException {
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
            throw new EventNotRegisteredException("No event type has been registered for event \"" + event.getEventTypeName() + "\".");
        }

        //Send valid event to Esper
        cepEventService.sendEventBean(event.getFieldValues(), event.getEventTypeName());
    }

    /**
     * Validates a given query string and checks whether it is syntactically and semantically valid.
     *
     * @param queryString The query string to check
     * @return The result of the validation wrapped in a validation object
     */
    @Override
    public CEPQueryValidation validateQuery(String queryString) {
        //Sanity check
        if ((queryString) == null || queryString.isEmpty()) {
            throw new IllegalArgumentException("Query string must not be null or empty.");
        }

        //Try to register a temporary statement with this query and check if this fails
        EsperCEPQuery testStatement = null;
        try {
            testStatement = createQuery("testQuery", queryString);
        } catch (EPException | EPCompileException e) {
            //Statement creation failed, query is not valid
            return new CEPQueryValidation(queryString, false, e.getMessage());
        } finally {
            //Destroy test statement in every case (if created)
            if (testStatement != null && testStatement.getCurrDeployment() != null) {
                try {
                    cepDeploymentService.undeploy(testStatement.getCurrDeployment().getDeploymentId());
                } catch (EPUndeployException e) {
                    e.printStackTrace();
                }

            }
        }

        //Validation successful
        return new CEPQueryValidation(queryString, true);
    }

    /**
     * Returns a list of all names of queries that are currently registered at the engine.
     *
     * @return The list of names
     */
    public List<String> getAllQueryNames() {
        //Get names
        String[] names = cepDeploymentService.getDeployments();

        //Convert array to list
        return Arrays.asList(names);
    }
}
