package de.ipvs.as.mbp.domain.rules;

import java.util.Objects;

import javax.persistence.GeneratedValue;

import de.ipvs.as.mbp.service.RuleTriggerDeleteValidator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Objects of this class represent triggers of rules that induce the execution of the containing rules. They consist out of a
 * CEP query string that is injected into a CEP engine. If the corresponding event pattern as described in the query
 * is detected by the engine, the actions of the containing rules may be executed subsequently.
 */
@MBPEntity(deleteValidator = RuleTriggerDeleteValidator.class)
@Document
public class RuleTrigger extends UserEntity {
    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    private String query;

    /**
     * Returns the id of the rule trigger.
     *
     * @return The id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id of the rule trigger.
     *
     * @param id The id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the rule trigger.
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the rule trigger.
     *
     * @param name The name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the description of the rule trigger.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the rule trigger.
     *
     * @param description The description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the CEP query string of the rule trigger.
     *
     * @return The CEP query string
     */
    public String getQuery() {
        return query;
    }

    /**
     * Sets the CEP query string of the rule trigger.
     *
     * @param query The CEP query string to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * Overrides the equals method by only considering the ids of rule triggers.
     *
     * @param o The object to compare
     * @return True, if both objects are identical (i.e. have the same id); false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RuleTrigger that = (RuleTrigger) o;
        return id.equals(that.id);
    }

    /**
     * Overrides the hash code method by using the id of rule triggers.
     *
     * @return The hash code of the object
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
