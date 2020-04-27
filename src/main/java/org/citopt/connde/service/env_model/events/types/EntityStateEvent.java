package org.citopt.connde.service.env_model.events.types;

import org.citopt.connde.domain.user_entity.UserEntity;

public class EntityStateEvent extends EnvironmentModelEvent {

    //Name of the event
    private static final String EVENT_NAME = "entity_update";

    private String nodeId;
    private UserEntity entity;
    private EntityState entityState;

    public EntityStateEvent(String nodeId, UserEntity entity, EntityState entityState) {
        this.nodeId = nodeId;
        this.entity = entity;
        this.entityState = entityState;
    }

    /**
     * Returns the name of the event, allowing to identify and recognize its type.
     *
     * @return The name of the event
     */
    @Override
    public String getName() {
        return EVENT_NAME;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public UserEntity getEntity() {
        return entity;
    }

    public void setEntity(UserEntity entity) {
        this.entity = entity;
    }

    public EntityState getEntityState() {
        return entityState;
    }

    public void setEntityState(EntityState entityState) {
        this.entityState = entityState;
    }
}
