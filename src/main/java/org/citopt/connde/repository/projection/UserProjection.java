package org.citopt.connde.repository.projection;

/**
 * Projection for users which may be useful to display user data that is permitted to be shown to other users.
 */
public interface UserProjection {
    String getId();

    String getUsername();
}
