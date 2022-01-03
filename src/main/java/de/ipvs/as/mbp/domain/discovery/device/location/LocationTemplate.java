package de.ipvs.as.mbp.domain.discovery.device.location;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ipvs.as.mbp.domain.user_entity.UserEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.json.JSONException;
import org.json.JSONObject;
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

    /**
     * Transforms the location template to a {@link JSONObject} that can be used as part of a location requirement
     * within a device description query in order to provide more details about the requirement. The transformation
     * happens by extending a provided {@link JSONObject} for necessary fields and finally returning the extended
     * {@link JSONObject} again. The type of the location template does not need to be explicitly added.
     *
     * @param jsonObject The {@link JSONObject} to extend
     * @return The resulting extended {@link JSONObject}
     * @throws JSONException In case a non-resolvable issue occurred during the transformation
     */
    @JsonIgnore
    public abstract JSONObject toQueryRequirementDetails(JSONObject jsonObject) throws JSONException;
}
