package de.ipvs.as.mbp.service.discovery.engine.tasks;

import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;

import java.util.concurrent.FutureTask;

/**
 * Objects of this class act as wrappers for {@link DiscoveryTask}s that can be directly scheduled by the
 * {@link DiscoveryEngine} as {@link FutureTask}s. However, in contrast to simple {@link FutureTask}, these wrappers
 * provide further features that may be useful for task management at the {@link DiscoveryEngine}.
 *
 * @param <T> The type of the wrapped discovery task
 */
public class TaskWrapper<T extends DiscoveryTask> extends FutureTask<Void> {

    //The discovery task to wrap
    private final T task;

    //Marks whether the task has already been started
    private boolean isStarted = false;

    /**
     * Creates a new {@link TaskWrapper} from a given {@link DiscoveryTask}.
     *
     * @param task The discovery task to wrap
     */
    public TaskWrapper(T task) {
        //Instantiate future task
        super(task, null);

        //Set fields
        this.task = task;
    }

    /**
     * Cancels the task.
     */
    public void cancel() {
        //Cancel the future task
        this.cancel(true);
    }

    /**
     * Returns the discovery task that underlies the wrapper.
     *
     * @return The discovery task
     */
    public T getTask() {
        return this.task;
    }

    /**
     * Returns whether the task has been marked as started.
     *
     * @return True, if the task has been marked as started; false otherwise
     */
    public boolean isStarted() {
        return this.isStarted;
    }

    /**
     * Marks the task as started.
     */
    public void setStarted() {
        this.isStarted = true;
    }
}
