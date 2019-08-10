package org.citopt.connde.domain.rules;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Map;

/**
 * Objects of this class represent actions of rules that are executed after at least one of the containing rules
 * was triggered.
 */
@Document
public class RuleAction {
    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    private RuleActionType type;

    private Map<String, String> parameters;

    /**
     * Returns the id of the rule action.
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the rule action.
     *
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the rule action.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the rule action.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the type of the rule action.
     *
     * @return The rule action type
     */
    public RuleActionType getType() {
        return type;
    }

    /**
     * Sets the type of the rule action.
     *
     * @param type The rule action type to set
     */
    public void setType(RuleActionType type) {
        this.type = type;
    }

    /**
     * Returns the parameters of the rule action.
     *
     * @return The parameters map (parameter name -> value)
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters of the rule action.
     *
     * @param parameters The parameters map (parameter name -> value) to set
     */
    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }
}
