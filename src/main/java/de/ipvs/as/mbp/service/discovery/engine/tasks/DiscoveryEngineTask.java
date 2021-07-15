package de.ipvs.as.mbp.service.discovery.engine.tasks;

import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;

import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * Interface for discovery-related tasks that can be scheduled by the {@link DiscoveryEngine} as
 * cancellable {@link FutureTask}s for asynchronous execution.
 *
 * @param <T> The return type of the task
 */
public interface DiscoveryEngineTask<T> extends Callable<T> {
    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     *
     * @return This task
     */
    T call();
}
