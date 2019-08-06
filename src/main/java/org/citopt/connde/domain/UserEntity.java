package org.citopt.connde.domain;

import org.citopt.connde.domain.user.User;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for entity classes that require user management. It adds support for
 * an owner user that owns a certain entity and is allowed to do everything with it
 * and a set of approved users that are allowed to access this entity as well.
 */
public abstract class UserEntity {

    //Owner of the entity
    @DBRef
    private User owner = null;

    //Approved users
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
}
