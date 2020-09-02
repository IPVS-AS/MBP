package org.citopt.connde.domain.user_entity;

import static org.citopt.connde.domain.user_entity.UserEntityRole.ADMIN;
import static org.citopt.connde.domain.user_entity.UserEntityRole.APPROVED_USER;
import static org.citopt.connde.domain.user_entity.UserEntityRole.ENTITY_OWNER;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.citopt.connde.DynamicBeanProvider;
import org.citopt.connde.domain.access_control.ACAttributeValue;
import org.citopt.connde.domain.access_control.ACPolicy;
import org.citopt.connde.domain.access_control.IACRequestedEntity;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.projection.UserExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.projection.ProjectionFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

/**
 * Base class for entity classes that require user management. It adds support for
 * an owner user that owns a certain entity and is allowed to do everything with it
 * and a set of approved users that are allowed to access this entity as well.
 */
public abstract class UserEntity implements IACRequestedEntity {
    // Default permission names
    private static final String PERMISSION_NAME_READ = "read";
    private static final String PERMISSION_NAME_DELETE = "delete";
    private static final String PERMISSION_NAME_APPROVE = "approve";
    private static final String PERMISSION_NAME_DISAPPROVE = "disapprove";

    // Defines the default policy for user entities
    protected static final UserEntityPolicy DEFAULT_POLICY = new UserEntityPolicy()
            .addPermission(PERMISSION_NAME_READ).addRole(APPROVED_USER).addRole(ADMIN)
            .addPermission(PERMISSION_NAME_DELETE).addRole(ENTITY_OWNER).addRole(ADMIN)
            .addPermission(PERMISSION_NAME_APPROVE).addRole(ENTITY_OWNER).addRole(ADMIN)
            .addPermission(PERMISSION_NAME_DISAPPROVE).addRole(ENTITY_OWNER).addRole(ADMIN)
            .lock();

    // The environment model for which the entity was created (null if none)
    @JsonIgnore
    @DBRef
    private EnvironmentModel environmentModel = null;

    // Owner of the entity
    @ACAttributeValue
    @JsonIgnore
    @DBRef
    private User owner = null;

    // Approved users
    @JsonIgnore
    @DBRef
    private Set<User> approvedUsers = new HashSet<>();
    
    /**
     * The list of access-control {@link ACPolicy policies} evaluated
     * before allowing access to this user entity.
     */
    @JsonIgnore
    @DBRef
    private List<ACPolicy> accessControlPolicies = new ArrayList<>();

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
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Returns the set of approved users.
     *
     * @return The approved users set
     */
    public Set<User> getApprovedUsers() {
        return approvedUsers;
    }
    
    @Override
    public List<ACPolicy> getAccessControlPolicies() {
    	return accessControlPolicies;
    }
    
    public void setAccessControlPolicies(List<ACPolicy> accessControlPolicies) {
		this.accessControlPolicies = accessControlPolicies;
	}

    /**
     * Sets the approved users set.
     *
     * @param approvedUsers The approved users set to set
     */
    public void setApprovedUsers(Set<User> approvedUsers) {
        this.approvedUsers = approvedUsers;
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
    public void setCreated(Date created) {
        this.created = created;
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
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Adds a given user to the set of approved users.
     *
     * @param user The user to approve
     */
    public void approveUser(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }
        this.approvedUsers.add(user);
    }

    /**
     * Removes a given user from the set of approved users.
     *
     * @param user The user to disapprove
     */
    public void disapproveUser(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }
        this.approvedUsers.remove(user);
    }

    /**
     * Removes all users from the set of approved users.
     */
    public void disapproveAll() {
        this.approvedUsers.clear();
    }

    /**
     * Returns whether a given user is owner of this entity.
     *
     * @param user The user to check
     * @return True, if the user is owner of this entity; false otherwise
     */
    public boolean isUserOwner(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        } else if (this.owner == null) {
            return false;
        }

        //Do check
        return this.owner.equals(user);
    }

    /**
     * Returns whether a given user is approved for this entity.
     *
     * @param user The user to check
     * @return True, if the user is approved for this entity; false otherwise
     */
    public boolean isUserApproved(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Do check
        return user.equals(this.owner) || this.approvedUsers.contains(user);
    }

    /**
     * Returns whether the entity was modelled as part of an environment model.
     *
     * @return True, if it was modelled; false otherwise
     */
    @JsonProperty("wasModelled")
    @ApiModelProperty(notes = "Whether the entity was modelled as part of an environment model", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean wasModelled() {
        return (environmentModel != null);
    }

    @JsonProperty("isOwning")
    @ApiModelProperty(notes = "Whether the current user is owner of the entity", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean isOwning() {
        //Resolve user service bean
        UserService userService = DynamicBeanProvider.get(UserService.class);

        //Get current user
        User currentUser = userService.getUserWithAuthorities();

        //Return whether current user is owner of this entity
        return isUserOwner(currentUser);
    }

    /**
     * Returns the name of the entities' owner.
     *
     * @return The name of the entity owner
     */
    @JsonProperty("ownerName")
    @ApiModelProperty(notes = "Name of the entity owner", example = "JohnDoe", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public String getOwnerName() {
        //Sanity check
        if (owner == null) {
            return null;
        }
        return owner.getUsername();
    }

    /**
     * Returns the set of users which are approved for this entity.
     *
     * @return The set of users
     */
    @JsonProperty("approvedUsers")
    @ApiModelProperty(notes = "List of users that are approved to work with this entity, only visible for the entity owner and admins", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public Set<UserExcerpt> getApprovedUsersProjection() {
        //Do not expose list if user is not allowed to approve
        if (!isApprovable()) {
            return null;
        }

        //Resolve projection factory bean
        ProjectionFactory projectionFactory = DynamicBeanProvider.get(ProjectionFactory.class);

        //List of approved users as projections
        Set<UserExcerpt> users = new HashSet<>();

        //Iterate over all approved users and create projections from them
        for (User user : approvedUsers) {
            UserExcerpt projection = projectionFactory.createProjection(UserExcerpt.class, user);
            users.add(projection);
        }

        return users;
    }

    /**
     * Returns whether the current user is permitted to delete this entity.
     *
     * @return True, if the user is permitted; false otherwise
     */
    @JsonProperty("isDeletable")
    @ApiModelProperty(notes = "Whether the current user is allowed to delete the entity", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean isDeletable() {
        return isPermitted("delete");
    }

    /**
     * Returns whether the current user is permitted to approve other users for this entity.
     *
     * @return True, if the user is permitted; false otherwise
     */
    @JsonProperty("isApprovable")
    @ApiModelProperty(notes = "Whether the current user is allowed to approve other users", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean isApprovable() {
        return isPermitted("approve");
    }

    /**
     * Returns whether the current user is permitted to disapprove other users for this entity.
     *
     * @return True, if the user is permitted; false otherwise
     */
    @JsonProperty("isDisapprovable")
    @ApiModelProperty(notes = "Whether the current user is allowed to disapprove other users", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean isDisapprovable() {
        return isPermitted("disapprove");
    }

    /**
     * Returns whether the current user is permitted to read other entities of this type.
     *
     * @return True, if the user is permitted; false otherwise
     */
    public boolean isReadable() {
        return isPermitted("read");
    }

    /**
     * Returns the user entity policy to use for evaluating user permissions. May be overridden by subclasses
     * in case a user entity policy different to the DEFAULT_POLICY is supposed to be used.
     *
     * @return The user entity policy to use
     */
    @JsonIgnore
    public UserEntityPolicy getUserEntityPolicy() {
        return DEFAULT_POLICY;
    }

    /**
     * Returns whether the current user has a certain permission-
     *
     * @param permissionName The name of the permission to check
     * @return True, if the user is permitted; false otherwise
     */
    private boolean isPermitted(String permissionName) {
        //Sanity check
        if ((permissionName == null) || permissionName.isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty.");
        }

        //Resolve user entity service bean
        UserEntityService userEntityService = DynamicBeanProvider.get(UserEntityService.class);

        //Check for permission
        return userEntityService.isUserPermitted(this, permissionName);
    }
}