package de.ipvs.as.mbp.service.user;

import java.util.Collections;
import java.util.Locale;
import java.util.Optional;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * Load a user from the database.
 * @author Imeri Amil
 */
@Component
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) {
        String lowercaseUsername = username.toLowerCase(Locale.ENGLISH);
        Optional<User> userFromDatabase = userRepository.findByUsername(lowercaseUsername);
        return userFromDatabase.map(user -> {
            return new org.springframework.security.core.userdetails.User(lowercaseUsername,
                user.getPassword(), Collections.emptyList());
        }).orElseThrow(() -> new UsernameNotFoundException("User " + lowercaseUsername + " was not found in the database"));
    }
}
