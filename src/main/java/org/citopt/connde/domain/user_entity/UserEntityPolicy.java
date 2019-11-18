package org.citopt.connde.domain.user_entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Objects of this class represent user entity policies that determines which user groups may access an user entity
 * or perform actions on it. The policy consists out of multiple permissions where a set of permitted user entity roles
 * is associated with each.
 */
public class UserEntityPolicy {
    //Maps permissions onto a set of approved user roles
    private HashMap<String, Set<UserEntityRole>> policyRules = new HashMap<>();

    //Remembers the last added permission for chaining
    private String lastPermission = null;

    //Allows to lock the policy in order to make it read-only
    private boolean locked = false;

    /**
     * Creates a new empty user entity policy.
     */
    UserEntityPolicy() {

    }

    /**
     * Clones a given user entity policy deeply into a new object. A possible lock on the origin policy will not
     * be copied, though.
     *
     * @param policy The user entity policy to clone
     */
    public UserEntityPolicy(UserEntityPolicy policy) {
        //Iterate over all policy rules of the origin policy
        for (String permission : policy.policyRules.keySet()) {
            //Get origin set of user roles
            Set<UserEntityRole> roleSet = policy.policyRules.get(permission);

            //Create shallow copy of role set
            Set<UserEntityRole> newRoleSet = new HashSet<>(roleSet);

            //Add permission and new role set to new policy rules
            this.policyRules.put(permission, newRoleSet);
        }

        this.lastPermission = policy.lastPermission;
    }

    /**
     * Adds a new permission without associated roles to the user entity policy.
     *
     * @param permissionName The name of the permission to add
     * @return The user entity policy for chaining
     */
    UserEntityPolicy addPermission(String permissionName) {
        //Check for lock
        if (this.locked) {
            throw new IllegalArgumentException("The user entity policy is locked and thus read-only.");
        }

        //Sanity check
        if ((permissionName == null) || permissionName.isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty.");
        }

        //Check if permission is already part of the policy
        else if (policyRules.containsKey(permissionName)) {
            throw new IllegalArgumentException("Permission has already been added.");
        }

        //Add new permission
        policyRules.put(permissionName, new HashSet<>());
        lastPermission = permissionName;

        return this;
    }

    /**
     * Adds a given user entity role to the last added permission of the user entity policy.
     *
     * @param role The user entity role to add
     * @return The user entity policy for chaining
     */
    UserEntityPolicy addRole(UserEntityRole role) {
        return addRole(lastPermission, role);
    }

    /**
     * Adds a given user entity role to a given existing permission of the user entity policy.
     *
     * @param permissionName The name of the permission to add the role to
     * @param role           he user entity role to add
     * @return The user entity policy for chaining
     */
    private UserEntityPolicy addRole(String permissionName, UserEntityRole role) {
        //Check for lock
        if (this.locked) {
            throw new IllegalArgumentException("The user entity policy is locked and thus read-only.");
        }

        //Sanity check
        if ((permissionName == null) || permissionName.isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty.");
        } else if (!policyRules.containsKey(permissionName)) {
            throw new IllegalArgumentException("Permission name is not part of the policy.");
        } else if (role == null) {
            throw new IllegalArgumentException("Role must not be null.");
        }

        //Get role set for permission
        Set<UserEntityRole> roleSet = policyRules.get(permissionName);

        //Add role to set
        roleSet.add(role);

        return this;
    }

    /**
     * Locks the user entity policy so that it becomes read-only and no further permissions and user roles may
     * be added.
     *
     * @return The user entity policy for chaining
     */
    public UserEntityPolicy lock() {
        this.locked = true;
        return this;
    }

    /**
     * Checks whether a user, given by a user entity role, is permitted to perform a certain action.
     *
     * @param permissionName The name of the permission to check
     * @param role           The user entity role to check
     * @return True, if the user is permitted; false otherwise
     */
    public boolean isPermitted(String permissionName, UserEntityRole role) {
        //Create set with role as only entry
        Set<UserEntityRole> roleSet = new HashSet<>();
        roleSet.add(role);

        return isPermitted(permissionName, roleSet);
    }

    /**
     * Checks whether a user, given by a set of user entity roles, is permitted to perform a certain action.
     *
     * @param permissionName The name of the permission to check
     * @param roles          The set of user entity roles to check
     * @return True, if the user is permitted; false otherwise
     */
    public boolean isPermitted(String permissionName, Set<UserEntityRole> roles) {
        //Sanity checks
        if ((permissionName == null) || permissionName.isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty.");
        } else if (roles == null) {
            throw new IllegalArgumentException("Roles list must not be null.");
        } else if (!policyRules.containsKey(permissionName)) {
            throw new IllegalArgumentException("There is no permission with this name in the policy.");
        }

        //Get permitted roles
        Set<UserEntityRole> permissionRoles = policyRules.get(permissionName);

        //Iterate over all provided roles and permission roles
        for (UserEntityRole providedRole : roles) {
            for (UserEntityRole permissionRole : permissionRoles) {
                //Check if the current provided role implies the permission role
                if (providedRole.implies(permissionRole)) {
                    //Permitted
                    return true;
                }
            }
        }

        //Not permitted
        return false;
    }

    /**
     * Returns whether the user entity policy contains a certain permission name or not.
     *
     * @param permissionName THe name of the permission to check
     * @return True, if the permission is contained; false otherwise
     */
    public boolean containsPermission(String permissionName) {
        //Sanity check
        if ((permissionName == null) || permissionName.isEmpty()) {
            throw new IllegalArgumentException("Permission name must not be null or empty.");
        }

        //Check for permission name
        return policyRules.containsKey(permissionName);
    }
}
