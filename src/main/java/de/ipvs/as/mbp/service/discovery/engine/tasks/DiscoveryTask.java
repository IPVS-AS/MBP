package de.ipvs.as.mbp.service.discovery.engine.tasks;

import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;

/**
 * Interface for discovery-related tasks that can be scheduled by the {@link DiscoveryEngine} in a
 * {@link TaskWrapper} for asynchronous execution.
 */
public interface DiscoveryTask extends Runnable {
    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    void run();
}
