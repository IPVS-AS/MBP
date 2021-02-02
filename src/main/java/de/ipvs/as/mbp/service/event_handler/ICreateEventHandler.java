package de.ipvs.as.mbp.service.event_handler;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;

/**
 * Interface for event handlers that are supposed to be triggered after the creation of an entity.
 *
 * @param <E> The type of user entities the event handler is responsible for
 */
public interface ICreateEventHandler<E extends UserEntity> {
    /**
     * Called in case an entity has been created and saved successfully.
     *
     * @param entity The created entity
     */
    void onCreate(E entity);
}
