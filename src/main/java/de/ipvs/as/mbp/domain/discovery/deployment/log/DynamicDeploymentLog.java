package de.ipvs.as.mbp.domain.discovery.deployment.log;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Objects of this class represent logs for {@link DynamicDeployment}s.
 */
@Document
public class DynamicDeploymentLog {
    @Id
    @GeneratedValue
    private String id;

    //ID of the dynamic deployment that is associated with the logs
    private String dynamicDeploymentId;

    //The entries of the log
    private List<DynamicDeploymentLogEntry> entries;

    /**
     * Creates a new, empty {@link DynamicDeploymentLog}.
     */
    public DynamicDeploymentLog() {
        //Initialize data structures
        this.entries = new LinkedList<>();
    }

    /**
     * Creates a new, empty {@link DynamicDeploymentLog} for a certain {@link DynamicDeployment}, given by its ID.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to create the log for
     */
    public DynamicDeploymentLog(String dynamicDeploymentId) {
        this();
    }

    /**
     * Returns the ID of the dynamic deployment log.
     *
     * @return The ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the dynamic deployment log.
     *
     * @param id The ID to set
     * @return The dynamic deployment log
     */
    private DynamicDeploymentLog setId(String id) {
        //Sanity check
        if ((id == null) || id.isEmpty()) {
            throw new IllegalArgumentException("The ID must not be null or empty.");
        }

        this.id = id;
        return this;
    }

    /**
     * Returns the ID of the {@link DynamicDeployment} that is associated with the log.
     *
     * @return The ID of the dynamic deployment
     */
    public String getDynamicDeploymentId() {
        return dynamicDeploymentId;
    }

    /**
     * Sets the ID of the {@link DynamicDeployment} that is associated with the log.
     *
     * @param dynamicDeploymentId The ID of the dynamic deployment to set
     * @return The dynamic deployment
     */
    public DynamicDeploymentLog setDynamicDeploymentId(String dynamicDeploymentId) {
        //Sanity check
        if ((dynamicDeploymentId == null) || dynamicDeploymentId.isEmpty()) {
            throw new IllegalArgumentException("The dynamic deployment ID must not be null or empty.");
        }

        this.dynamicDeploymentId = dynamicDeploymentId;
        return this;
    }

    /**
     * Returns all {@link DynamicDeploymentLogEntry}s that are available within the log.
     *
     * @return The log entries
     */
    public List<DynamicDeploymentLogEntry> getEntries() {
        return entries;
    }

    /**
     * Sets the {@link DynamicDeploymentLogEntry}s that are supposed to be available within the log.
     *
     * @param entries The log entries to set
     * @return The dynamic deployment log
     */
    public DynamicDeploymentLog setEntries(List<DynamicDeploymentLogEntry> entries) {
        //Null checks
        if ((entries == null) || entries.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The log entries must not be null or empty.");
        }

        this.entries = entries;
        return this;
    }

    /**
     * Adds a given {@link DynamicDeploymentLogEntry} to the log.
     *
     * @param entry The entry to add
     * @return The dynamic deployment log
     */
    public DynamicDeploymentLog addEntry(DynamicDeploymentLogEntry entry) {
        //Null check
        if (entry == null) {
            throw new IllegalArgumentException("The entry must not be null.");
        }

        //Add entry
        this.entries.add(entry);
        return this;
    }

    /**
     * Adds a given {@link Collection} of {@link DynamicDeploymentLogEntry}s to the log.
     *
     * @param entries The collection of entries to add
     * @return The dynamic deployment log
     */
    public DynamicDeploymentLog addEntries(Collection<DynamicDeploymentLogEntry> entries) {
        //Null checks
        if ((entries == null) || entries.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The entries must not be null.");
        }

        //Add entries
        this.entries.addAll(entries);
        return this;
    }
}
