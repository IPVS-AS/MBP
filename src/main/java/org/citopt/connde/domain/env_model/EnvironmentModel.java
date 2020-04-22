package org.citopt.connde.domain.env_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Document class for environment model entities, created by the environment modelling tool.
 */
@Document
public class EnvironmentModel extends UserEntity {

    @Id
    @GeneratedValue
    private String id;

    @NotNull
    @Indexed(unique = true)
    private String name;

    private String description;

    private String modelJSON;

    //Set of entities that were created for the model
    @JsonIgnore
    private Set<UserEntity> entitySet;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModelJSON() {
        return modelJSON;
    }

    public void setModelJSON(String modelJSON) {
        this.modelJSON = modelJSON;
    }


    public Set<UserEntity> getEntitySet() {
        return entitySet;
    }

    public void setEntitySet(Set<UserEntity> entitySet) {
        this.entitySet = entitySet;
    }
}
