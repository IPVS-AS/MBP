package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user.UserLoginData;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class UserAuthentication implements Authentication {

    private User user;

    public UserAuthentication(User user) {
        setUser(user);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public Object getCredentials() {
        return new UserLoginData();
    }

    @Override
    public Object getDetails() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean authenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException("This authentication object is always authenticated.");
    }

    @Override
    public String getName() {
        return user.getUsername();
    }

    /**
     * Sets the user object to which this authentication belongs to.
     *
     * @param user The user to set
     */
    private void setUser(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Set user
        this.user = user;
    }
}
