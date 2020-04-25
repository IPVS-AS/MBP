package org.citopt.connde.service.env_model.events.types;

public interface EnvironmentModelEvent {
    /**
     * Returns the name of the event, allowing to identify and recognize its type.
     *
     * @return The name of the event
     */
    String getName();
}
