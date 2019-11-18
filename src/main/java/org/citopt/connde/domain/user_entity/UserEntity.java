package org.citopt.connde.domain.user_entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import org.citopt.connde.DynamicBeanProvider;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.projection.UserExcerpt;
import org.citopt.connde.service.UserEntityService;
import org.citopt.connde.service.UserService;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.projection.ProjectionFactory;

import java.util.HashSet;
import java.util.Set;

import static org.citopt.connde.domain.user_entity.UserEntityRole.*;

/**
 * Base class for entity classes that require user management. It adds support for
 * an owner user that owns a certain entity and is allowed to do everything with it
 * and a set of approved users that are allowed to access this entity as well.
 */
public abstract class UserEntity {

    private static final String PERMISSION_NAME_CREATE = "create";
    private static final String PERMISSION_NAME_READ = "read";
    private static final String PERMISSION_NAME_DELETE = "delete";
    private static final String PERMISSION_NAME_APPROVE = "approve";
    private static final String PERMISSION_NAME_DISAPPROVE = "disapprove";

    //Defines the default policy for user entities
    protected static final UserEntityPolicy DEFAULT_POLICY = new UserEntityPolicy()
            .addPermission(PERMISSION_NAME_CREATE).addRole(USER)
            .addPermission(PERMISSION_NAME_READ).addRole(APPROVED_USER).addRole(ADMIN)
            .addPermission(PERMISSION_NAME_DELETE).addRole(ENTITY_OWNER).addRole(ADMIN)
            .addPermission(PERMISSION_NAME_APPROVE).addRole(ENTITY_OWNER).addRole(ADMIN)
            .addPermission(PERMISSION_NAME_DISAPPROVE).addRole(ENTITY_OWNER).addRole(ADMIN)
            .lock();

    //Owner of the entity
    @JsonIgnore
    @DBRef
    private User owner = null;

    //Approved users
    @JsonIgnore
    @DBRef
    private Set<User> approvedUsers = new HashSet<>();

    /**
     * Returns the owner user of this entity.
     *
     * @return The owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Sets the owner user of this entity.
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

    /**
     * Sets the approved users set.
     *
     * @param approvedUsers The approved users set to set
     */
    public void setApprovedUsers(Set<User> approvedUsers) {
        this.approvedUsers = approvedUsers;
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

    @JsonProperty("isOwner")
    @ApiModelProperty(notes = "Whether the current user is owner of the entity", accessMode = ApiModelProperty.AccessMode.READ_ONLY, readOnly = true)
    public boolean isOwner() {
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
        return isPermitted("create");
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
     * Returns whether the current user is permitted to create other entities of this type.
     *
     * @return True, if the user is permitted; false otherwise
     */
    public boolean isCreatable() {
        return isPermitted("create");
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
        return userEntityService.isUserPermitted("delete", this);
    }
}