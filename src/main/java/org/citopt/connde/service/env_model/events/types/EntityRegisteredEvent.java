package org.citopt.connde.service.env_model.events.types;

import org.citopt.connde.domain.user_entity.UserEntity;

public class EntityRegisteredEvent extends EnvironmentModelEvent {
    //Name of the event
    private static final String EVENT_NAME = "entity_registered";

    private String nodeId;
    private UserEntity entity;

    public EntityRegisteredEvent(String nodeId, UserEntity entity) {
        this.nodeId = nodeId;
        this.entity = entity;
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
}
