package org.citopt.connde.domain.entity_type;

import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.GeneratedValue;

/**
 * Super class for certain types of entities.
 */
public abstract class EntityType extends UserEntity {
    @Id
    @GeneratedValue
    private String id;

    @Indexed
    private String name;

    @Indexed
    private String icon;

    /**
     * Returns the class of the user entity for which this type is for.
     *
     * @return The class of the user entity
     */
    public abstract Class<? extends UserEntity> getEntityClass();
}
