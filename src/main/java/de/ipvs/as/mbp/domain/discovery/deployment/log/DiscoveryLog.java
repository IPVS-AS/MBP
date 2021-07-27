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
 * Objects of this class represent logs for {@link DynamicDeployment}s and consist out of a list of
 * {@link DiscoveryLogEntry}s collecting the individual {@link DiscoveryLogMessage}s.
 */
@Document
public class DiscoveryLog {
    @Id
    @GeneratedValue
    private String id;

    //ID of the dynamic deployment that is associated with the log
    private String dynamicDeploymentId;

    //The entries of the log
    private List<DiscoveryLogEntry> entries;

    /**
     * Creates a new, empty {@link DiscoveryLog}.
     */
    public DiscoveryLog() {
        //Initialize data structures
        this.entries = new LinkedList<>();
    }

    /**
     * Creates a new, empty {@link DiscoveryLog} for a certain {@link DynamicDeployment}, given by its ID.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to create the log for
     */
    public DiscoveryLog(String dynamicDeploymentId) {
        this();
    }

    /**
     * Returns the ID of the {@link DiscoveryLog}.
     *
     * @return The ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the {@link DiscoveryLog}.
     *
     * @param id The ID to set
     * @return The {@link DiscoveryLog}
     */
    private DiscoveryLog setId(String id) {
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
     * @return The ID of the {@link DynamicDeployment}
     */
    public String getDynamicDeploymentId() {
        return dynamicDeploymentId;
    }

    /**
     * Sets the ID of the {@link DynamicDeployment} that is associated with the log.
     *
     * @param dynamicDeploymentId The ID of the {@link DynamicDeployment} to set
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog setDynamicDeploymentId(String dynamicDeploymentId) {
        //Sanity check
        if ((dynamicDeploymentId == null) || dynamicDeploymentId.isEmpty()) {
            throw new IllegalArgumentException("The dynamic deployment ID must not be null or empty.");
        }

        this.dynamicDeploymentId = dynamicDeploymentId;
        return this;
    }

    /**
     * Returns a list of all {@link DiscoveryLogEntry}s that are available within the log.
     *
     * @return The list of log entries
     */
    public List<DiscoveryLogEntry> getEntries() {
        return entries;
    }

    /**
     * Sets the list of {@link DiscoveryLogEntry}s that are supposed to be available within the log.
     *
     * @param entries The list of {@link DiscoveryLogEntry}s to set
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog setEntries(List<DiscoveryLogEntry> entries) {
        //Null checks
        if ((entries == null) || entries.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The log entries must not be null or empty.");
        }

        this.entries = entries;
        return this;
    }

    /**
     * Adds a given {@link DiscoveryLogEntry} to the log.
     *
     * @param entry The {@link DiscoveryLogEntry} to add
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog addEntry(DiscoveryLogEntry entry) {
        //Null check
        if (entry == null) {
            throw new IllegalArgumentException("The entry must not be null.");
        }

        //Add entry
        this.entries.add(entry);
        return this;
    }

    /**
     * Adds a given {@link Collection} of {@link DiscoveryLogEntry}s to the log.
     *
     * @param entries The collection of {@link DiscoveryLogEntry}s to add
     * @return The {@link DiscoveryLog}
     */
    public DiscoveryLog addEntries(Collection<DiscoveryLogEntry> entries) {
        //Null checks
        if ((entries == null) || entries.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The entries must not be null.");
        }

        //Add entries
        this.entries.addAll(entries);
        return this;
    }
}
