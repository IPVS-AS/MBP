package de.ipvs.as.mbp.security;

import de.ipvs.as.mbp.domain.user.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Collection of utility functions for Spring Security.
 */
public final class SecurityUtils {
    /**
     * Get the username of the current user.
     *
     * @return the username of the current user
     */
    public static String getCurrentUserUsername() {
        //Get security context
        SecurityContext securityContext = SecurityContextHolder.getContext();

        //Get current authentication from security context
        Authentication authentication = securityContext.getAuthentication();
        if (authentication == null) {
            return null;
        }

        //Get user from authentication
        User user;
        try {
            user = (User) authentication.getDetails();
        } catch (ClassCastException cce) {
            cce.printStackTrace();
            return null;
        }
        //Extract username
        return user.getUsername();
    }

    /**
     * Check if a user is authenticated.
     *
     * @return true if the user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        //Get security context
        SecurityContext securityContext = SecurityContextHolder.getContext();

        //Get current authentication from security context
        Authentication authentication = securityContext.getAuthentication();
        return (authentication != null);
    }

}
