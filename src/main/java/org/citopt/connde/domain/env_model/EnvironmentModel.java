package org.citopt.connde.domain.env_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

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

    //Entities that were created for the model (node id -> entity)
    @JsonIgnore
    private Map<String, UserEntity> entityMap = new HashMap<>();

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

    public Map<String, UserEntity> getEntityMap() {
        return entityMap;
    }

    public void setEntityMap(Map<String, UserEntity> entityMap) {
        this.entityMap = entityMap;
    }
}
