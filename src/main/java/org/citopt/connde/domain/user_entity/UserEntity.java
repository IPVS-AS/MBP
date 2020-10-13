package org.citopt.connde.domain.user_entity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.citopt.connde.domain.access_control.ACAttributeValue;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.access_control.IACRequestedEntity;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.domain.user.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * Base class for entity classes that require user management. It adds support for
 * an owner user that owns a certain entity and is allowed to do everything with it
 * and a set of approved users that are allowed to access this entity as well.
 */
public abstract class UserEntity implements IACRequestedEntity {

	@ACAttributeValue
    @JsonIgnore
    @DBRef
    private EnvironmentModel environmentModel = null;

    // Owner of the entity
    @ACAttributeValue
//    @JsonIgnore
    @DBRef
    private User owner = null;

    /**
     * The list of access-control {@link ACPolicy} ids evaluated
     * before allowing access to this user entity.
     */
    private List<String> accessControlPolicyIds = new ArrayList<>();

    // Creation date, managed by Mongo Auditing
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreatedDate
    private Date created;

    // Date of the last modification, managed by Mongo Auditing
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @LastModifiedDate
    private Date lastModified;

    /**
     * Returns the environment model for which the entity was created. If it has not been created
     * for an environment model, null is returned.
     *
     * @return The environment model
     */
    public EnvironmentModel getEnvironmentModel() {
        return environmentModel;
    }

    /**
     * Sets the environment model for which the entity was created. If null is passed,
     * the entity was not created for an environment model.
     *
     * @param environmentModel The environment model to set
     */
    public void setEnvironmentModel(EnvironmentModel environmentModel) {
        this.environmentModel = environmentModel;
    }

    /**
     * Returns the owner of this entity.
     *
     * @return The owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Sets the owner of this entity.
     *
     * @param owner The owner to set
     */
    public UserEntity setOwner(User owner) {
        this.owner = owner;
		return this;
    }

    @Override
    public List<String> getAccessControlPolicyIds() {
		return accessControlPolicyIds;
	}
    
    public UserEntity setAccessControlPolicyIds(List<String> accessControlPolicyIds) {
		this.accessControlPolicyIds = accessControlPolicyIds;
		return this;
	}

    /**
     * Returns the creation date of the entity.
     *
     * @return The creation date
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the creation date of the entity.
     *
     * @param created The creation date to set
     */
    public UserEntity setCreated(Date created) {
        this.created = created;
		return this;
    }

    /**
     * Returns the date of the last modification of the entity.
     *
     * @return The modification date
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the date of the last modification of the entity.
     *
     * @param lastModified The modification date to set
     */
    public UserEntity setLastModified(Date lastModified) {
        this.lastModified = lastModified;
		return this;
    }

    /**
     * Returns whether a given user is owner of this entity.
     *
     * @param user The user to check
     * @return True, if the user is owner of this entity; false otherwise
     */
    public boolean isUserOwner(User user) {
        // Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        } else if (this.owner == null) {
            return false;
        }

        // Do check
        return this.owner.equals(user);
    }

    /**
     * Returns whether the entity was modelled as part of an environment model.
     *
     * @return True, if it was modelled; false otherwise
     */
    @JsonProperty("wasModelled")
    @ApiModelProperty(notes = "Whether the entity was modelled as part of an environment model", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    public boolean wasModelled() {
        return (environmentModel != null);
    }

    /**
     * Returns the name of the entities' owner.
     *
     * @return The name of the entity owner
     */
    @JsonProperty("ownerName")
    @ApiModelProperty(notes = "Name of the entity owner", example = "JohnDoe", accessMode = ApiModelProperty.AccessMode.READ_ONLY)
    public String getOwnerName() {
        //Sanity check
        if (owner == null) {
            return null;
        }
        return owner.getUsername();
    }

}