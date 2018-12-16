package org.citopt.connde.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.user.Authority;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.AuthorityRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for managing users.
 * @author Imeri Amil
 */
@Service
public class UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    public User createUser(String username, String password, String firstName, String lastName) {
        User newUser = new User();
        Authority authority = authorityRepository.findOne(Constants.USER);
        Set<Authority> authorities = new HashSet<>();
        String encryptedPassword = passwordEncoder.encode(password);
        newUser.setUsername(username);
        // new user gets initially a generated password
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        return newUser;
    }
    
    public boolean passwordMatches(String userPassword, String userFromDatabasePassword){
    	return passwordEncoder.matches(userPassword, userFromDatabasePassword);
    }
    
    public User createUser(User user) {
    	Authority authority = authorityRepository.findOne(Constants.USER);
    	Set<Authority> authorities = new HashSet<>();
    	authorities.add(authority);
    	user.setAuthorities(authorities);
    	user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return user;
    }

    public void updateUser(String firstName, String lastName) {
        userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername()).ifPresent(user -> {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            userRepository.save(user);
        });
    }

    public void updateUser(String id, String username, String password, String firstName, String lastName, Set<Authority> authorities) {
        Optional.of(userRepository
            .findOne(id))
            .ifPresent(user -> {
                user.setUsername(username);
                user.setPassword(passwordEncoder.encode(password));
                user.setFirstName(firstName);
                user.setLastName(lastName);
                Set<Authority> managedAuthorities = user.getAuthorities();
                managedAuthorities.clear();
                authorities.forEach(
                    authority -> managedAuthorities.add(authorityRepository.findOne(authority.getName()))
                );
                userRepository.save(user);
            });
    }

    public void deleteUser(String username) {
        userRepository.findOneByUsername(username).ifPresent(user -> {
            userRepository.delete(user);
        });
    }

    public void changePassword(String password) {
        userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername()).ifPresent(user -> {
            String encryptedPassword = passwordEncoder.encode(password);
            user.setPassword(encryptedPassword);
            userRepository.save(user);
        });
    }

    public Optional<User> getUserWithAuthoritiesByUsername(String username) {
        return userRepository.findOneByUsername(username);
    }

    public User getUserWithAuthorities(String id) {
        User user = userRepository.findOne(id);
        return user;
    }

    public User getUserWithAuthorities() {
        Optional<User> optionalUser = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername());
        User user = null;
        if (optionalUser.isPresent()) {
          user = optionalUser.get();
         }
         return user;
    }

}