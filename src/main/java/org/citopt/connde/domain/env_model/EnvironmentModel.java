package org.citopt.connde.domain.env_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;
import javax.validation.constraints.NotNull;
import java.util.Date;
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

    //Maps ids of entities (devices, sensors, ...) to the corresponding entity objects
    @JsonIgnore
    private Map<String, UserEntity> entityMapping;

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


    public Map<String, UserEntity> getEntityMapping() {
        return entityMapping;
    }

    public void setEntityMapping(Map<String, UserEntity> entityMapping) {
        this.entityMapping = entityMapping;
    }
}
