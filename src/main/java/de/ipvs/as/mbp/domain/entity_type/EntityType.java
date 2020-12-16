package de.ipvs.as.mbp.domain.entity_type;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

import javax.persistence.GeneratedValue;

/**
 * Super class for types of entities.
 */
public abstract class EntityType extends UserEntity {
    @Id
    @GeneratedValue
    protected String id;

    @Indexed
    protected String name;

    private EntityTypeIcon icon;

    public EntityType() {

    }

    public EntityType(String name) {
        this.name = name;
    }

    /**
     * Returns the class of the user entity for which this type is for.
     *
     * @return The class of the user entity
     */
    public abstract Class<? extends UserEntity> getEntityClass();

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EntityType setName(String name) {
        this.name = name;
        return this;
    }

    public EntityTypeIcon getIcon() {
        return icon;
    }

    public EntityType setIcon(EntityTypeIcon icon) {
        this.icon = icon;
        return this;
    }
}
