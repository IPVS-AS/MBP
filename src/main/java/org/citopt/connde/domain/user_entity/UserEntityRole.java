package org.citopt.connde.domain.user_entity;

/**
 * Enumeration of possible user roles in terms of user entities. Each user role may hold a set of implications which
 * represent a "is-a" relationship. For example, the approved user role is derived from the user role,
 * because each approved user is also a user. This allows the propagation through the hierarchy.
 */
public enum UserEntityRole {
    ANONYMOUS,
    USER(new UserEntityRole[]{ANONYMOUS}),
    APPROVED_USER(new UserEntityRole[]{USER}),
    ENTITY_OWNER(new UserEntityRole[]{APPROVED_USER}),
    ADMIN(new UserEntityRole[]{USER});

    private UserEntityRole[] implications;

    /**
     * Creates a new user entity role without any implications.
     */
    UserEntityRole() {
        implications = new UserEntityRole[]{};
    }

    /**
     * Creates a new user entity role with a list of user entity roles that are implied by this role.
     *
     * @param implications The roles that are implied by this role
     */
    UserEntityRole(UserEntityRole[] implications) {
        //Sanity check
        if (implications == null) {
            throw new IllegalArgumentException("The list of implications must not be null.");
        }
        this.implications = implications;
    }

    /**
     * Checks whether this role implies a given role recursively.
     *
     * @param role The role to check
     * @return True, if this role implies the given role; false otherwise
     */
    public boolean implies(UserEntityRole role) {
        //Sanity check
        if (role == null) {
            throw new IllegalArgumentException("Role argument must not be null.");
        }

        //Trivial check
        if (this.equals(role)) {
            return true;
        }

        //Check each implication of this role recursively
        for (UserEntityRole roleToCheck : this.implications) {
            if (roleToCheck.implies(role)) {
                return true;
            }
        }

        //No implication found
        return false;
    }
}
