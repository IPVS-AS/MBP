package org.citopt.connde.domain.rules;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;

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
}
