package de.ipvs.as.mbp.domain.rules;

import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.GeneratedValue;
import java.util.*;

/**
 * Objects of this class represent triggers of rules that induce the execution of the containing rules. They consist out of a
 * CEP query string that is injected into a CEP engine. If the corresponding event pattern as described in the query
 * is detected by the engine, the actions of the containing rules may be executed subsequently.
 */
@MBPEntity(createValidator = RuleTriggerCreateValidator.class, deleteValidator = RuleTriggerDeleteValidator.class)
public class RuleTrigger extends UserEntity {
    @Id
    @GeneratedValue
    private String id;

    @Indexed(unique = true)
    private String name;

    private String description;

    /**
     * This map stores all event type aliases and their corresponding component. This map
     * is only set automatically right if event name delcarations are provided in a pattern format like:
     * every(event_0=sensor_60e6d6f32361155cc5b7f21c and event_1=sensor_60e6d6fe2361155cc5b7f21d)
     */
    private Map<String, String> eventNameToComponentMapping;

    private String query;

    /**
     * This map stores all event type aliases and their corresponding component. This map
     * is only set automatically right if event name delcarations are provided in a pattern format like:
     * every(event_0=sensor_60e6d6f32361155cc5b7f21c and event_1=sensor_60e6d6fe2361155cc5b7f21d)
     *
     * @return
     */
    public Map<String, String> getEventNameToComponentMapping() {
        return eventNameToComponentMapping;
    }

    public void setEventNameToComponentMapping(Map<String, String> eventNameToComponentMapping) {
        this.eventNameToComponentMapping = eventNameToComponentMapping;
    }

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
        setEventNameToComponentMapping(query);
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

    /**
     * Uses the EPL Select query to retrieve a mapping of event name alias to componentID of the atomic
     * event.
     * @param query The EPL SELECT query.
     */
    private void setEventNameToComponentMapping(String query) {
        eventNameToComponentMapping = new HashMap<>();

        String declarationPart = StringUtils.substringBetween(query, "every(", ")]");
        String[] singleEventParts = declarationPart.split("\\sor\\s|\\sand\\s|\\s->\\s");

        for (int i = 0; i < singleEventParts.length; i++) {
            singleEventParts[i] = singleEventParts[i].trim();
            singleEventParts[i] = singleEventParts[i].replaceAll("\\(.*\\)", "");
            String[] parts = singleEventParts[i].split("=");
            eventNameToComponentMapping.put(parts[0], parts[1].split("_")[1]);
        }
    }
}
