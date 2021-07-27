package de.ipvs.as.mbp.service.discovery.engine.tasks;

import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogEntry;
import de.ipvs.as.mbp.domain.discovery.deployment.log.DiscoveryLogMessage;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;

/**
 * Interface for discovery-related tasks that can be scheduled by the {@link DiscoveryEngine} in a
 * {@link TaskWrapper} for asynchronous execution. The tasks are supposed to be designed in a way that
 * they terminate quickly in case the notice that their execution is not necessary or not desired anymore.
 */
public interface DiscoveryTask extends Runnable {
    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    void run();

    /**
     * Returns the {@link DiscoveryLogEntry} that is used within this task in order to collect
     * {@link DiscoveryLogMessage}s for logging purposes. May be null, if the task does not perform logging.
     *
     * @return The {@link DiscoveryLogEntry} or null, if logging is not performed
     */
    DiscoveryLogEntry getLogEntry();

    /**
     * Returns a simple, short and human-readable description of the task.
     *
     * @return The human-readable description
     */
    String toHumanReadableString();
}
