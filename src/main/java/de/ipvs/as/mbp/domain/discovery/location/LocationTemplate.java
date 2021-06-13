package de.ipvs.as.mbp.domain.discovery.location;

import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.GeneratedValue;

/**
 * Abstract base class for location templates.
 */
@Document
@ApiModel(description = "Model for location templates")
public abstract class LocationTemplate extends UserEntity {
    @Id
    @GeneratedValue
    @ApiModelProperty(notes = "Location template ID", example = "5c8f7ad66f9e3c1bacb0fa99", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    private String id;

    @ApiModelProperty(notes = "Location template name", example = "My home", required = true)
    private String name;

    public LocationTemplate() {

    }

    public String getId() {
        return id;
    }

    public LocationTemplate setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public LocationTemplate setName(String name) {
        this.name = name;
        return this;
    }
}
