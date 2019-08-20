package org.citopt.connde.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.citopt.connde.DynamicBeanProvider;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.projection.UserProjection;
import org.citopt.connde.service.UserService;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.projection.ProjectionFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for entity classes that require user management. It adds support for
 * an owner user that owns a certain entity and is allowed to do everything with it
 * and a set of approved users that are allowed to access this entity as well.
 */
public abstract class UserEntity {

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

    /**
     * Returns the name of the entities' owner.
     *
     * @return The name of the entity owner
     */
    @JsonProperty("ownerName")
    public String getOwnerName() {
        //Sanity check
        if (owner == null) {
            return null;
        }
        return owner.getUsername();
    }

    @JsonProperty("isApprovable")
    public boolean isApprovable() {
        //Resolve user service bean
        UserService userService = DynamicBeanProvider.get(UserService.class);

        //Get current user
        User currentUser = userService.getUserWithAuthorities();

        //Return whether current user is owner of this entity
        return isUserOwner(currentUser) || currentUser.isAdmin();
    }

    @JsonProperty("approvedUsers")
    public Set<UserProjection> getApprovedUsersProjection() {
        //Do not expose list of approved users if user is not approvable
        if (!isApprovable() || (approvedUsers == null)) {
            return null;
        }

        //Resolve projection factory bean
        ProjectionFactory projectionFactory = DynamicBeanProvider.get(ProjectionFactory.class);

        //List of approved users as projections
        Set<UserProjection> users = new HashSet<>();

        //TODO REMOVE!!!!!
        User testUser = new User();
        testUser.setId("123456789asdf");
        testUser.setUsername("test_user");
        testUser.setFirstName("Max");
        testUser.setLastName("Mustermann");
        testUser.setPassword("12345");
        approvedUsers.add(testUser);

        //Iterate over all approved users and create projections from them
        for (User user : approvedUsers) {
            UserProjection projection = projectionFactory.createProjection(UserProjection.class, user);
            users.add(projection);
        }

        //TODO REMOVE AS WELL!!!!!
        approvedUsers.remove(testUser);

        return users;
    }
}