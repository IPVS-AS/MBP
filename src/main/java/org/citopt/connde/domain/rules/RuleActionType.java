package org.citopt.connde.domain.rules;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.citopt.connde.service.rules.execution.ExecutorProvider;
import org.citopt.connde.service.rules.execution.RuleActionExecutor;
import org.citopt.connde.service.rules.execution.actuator_action.ActuatorActionExecutor;
import org.citopt.connde.service.rules.execution.component_deployment.ComponentDeploymentExecutor;
import org.citopt.connde.service.rules.execution.ifttt_webhook.IFTTTWebhookExecutor;

/**
 * Enumeration of available rule action types. Each rule action type maps to a rule executor object which then takes
 * care of executing the action.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum RuleActionType {
    ACTUATOR_ACTION("Actuator action", ExecutorProvider.get(ActuatorActionExecutor.class)),
    IFTTT_WEBHOOK("IFTTT webhook", ExecutorProvider.get(IFTTTWebhookExecutor.class)),
    COMPONENT_DEPLOYMENT("Component deployment", ExecutorProvider.get(ComponentDeploymentExecutor.class));

    private String id;
    private String name;

    @JsonIgnore
    private RuleActionExecutor executor;

    /**
     * Creates a new rule action type, mapping to a certain rule action executor which takes care of executing
     * actions of this type.
     *
     * @param name     The name of the rule action type
     * @param executor The rule action executor to use
     */
    RuleActionType(String name, RuleActionExecutor executor) {
        //Sanity checks
        if ((name == null) || name.isEmpty()) {
            throw new IllegalArgumentException("Name must not be null or empty.");
        } else if (executor == null) {
            throw new IllegalArgumentException("Executor must not be null.");
        }
        this.id = toString();
        this.name = name;
        this.executor = executor;
    }

    /**
     * Returns the id of the rule action type.
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the name of the rule action type.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the rule action executor of the rule action type.
     *
     * @return The rule action executor
     */
    public RuleActionExecutor getExecutor() {
        return executor;
    }


    /**
     * Returns the rule action type that corresponds to a certain type enum name. This method is called when
     * the client uses the enum name of a rule action in its request that needs to be mapped to an actual
     * rule action type object.
     *
     * @param name The enum name of the rule action type
     * @return The corresponding rule action type
     */
    @JsonCreator
    public static RuleActionType create(String name) {
        //Check for invalid enum name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available rule action type to the provided enum name
        for (RuleActionType actionType : values()) {
            if (name.equals(actionType.toString())) {
                //Rule action type found
                return actionType;
            }
        }

        //No matching rule action type was found
        return null;
    }
}
