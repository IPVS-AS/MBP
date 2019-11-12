package org.citopt.connde.service;

import org.citopt.connde.constants.Constants;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.domain.user.Authority;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntityRole;
import org.citopt.connde.repository.AuthorityRepository;
import org.citopt.connde.repository.UserEntityRepository;
import org.citopt.connde.repository.UserRepository;
import org.citopt.connde.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service class for managing users.
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
        newUser.setPassword(encryptedPassword);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        authorities.add(authority);
        newUser.setAuthorities(authorities);
        userRepository.save(newUser);
        return newUser;
    }

    public boolean passwordMatches(String userPassword, String userFromDatabasePassword) {
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
        return userRepository.findOne(id);
    }

    public User getUserWithAuthorities() {
        Optional<User> optionalUser = userRepository.findOneByUsername(SecurityUtils.getCurrentUserUsername());
        User user = null;
        if (optionalUser.isPresent()) {
            user = optionalUser.get();
        }
        return user;
    }

    /**
     * Returns a list of user entity roles.
     * @return
     */
    public UserEntityRole[] getUserEntityRoles(){
        //TODO
        return null;
    }

    /**
     * Returns a user entity with a certain id from a given repository that is owned by the current user or where the
     * user is among the set of approved users.
     *
     * @param repository The repository to retrieve the user entity from
     * @param entityId   The id of the entity to retrieve
     * @return The user entity or null if it could not be found or if the user has no permission to access it
     */
    public UserEntity getUserEntityFromRepository(UserEntityRepository repository, String entityId) {
        //Get current user
        User user = getUserWithAuthorities();

        //Get corresponding user entities from repository
        return getUserEntityFromRepository(repository, entityId, user);
    }


    /**
     * Returns a user entity with a certain id from a given repository that is owned by a given user or where the
     * user is among the set of approved users.
     *
     * @param repository The repository to retrieve the user entity from
     * @param entityId   The id of the entity to retrieve
     * @param user       The user for which the user entity is supposed to be retrieved
     * @return The user entity or null if it could not be found or if the user has no permission to access it
     */
    public UserEntity getUserEntityFromRepository(UserEntityRepository repository, String entityId, User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Get user entity from repository
        UserEntity entities = (UserEntity) repository.findOne(entityId);

        //Check for null (not found)
        if (entities == null) {
            return null;
        }

        //Return entity if user is admin or approved
        if (user.isAdmin() || entities.isUserApproved(user)) {
            return entities;
        }

        //User is not approved
        return null;
    }

    /**
     * Returns a list of user entities from a given repository that are owned by the current user or where the
     * user is among the set of approved users.
     *
     * @param repository The repository to retrieve the user entities from
     * @return List of user entities
     */
    public List<UserEntity> getUserEntitiesFromRepository(UserEntityRepository repository) {
        //Get current user
        User user = getUserWithAuthorities();

        //Get corresponding user entities from repository
        return getUserEntitiesFromRepository(repository, user);
    }


    /**
     * Returns a list of user entities from a given repository that are owned by a given user or where the
     * user is among the set of approved users.
     *
     * @param repository The repository to retrieve the user entities from
     * @param user       The user for which the user entities are supposed to be retrieved
     * @return List of user entities
     */
    public List<UserEntity> getUserEntitiesFromRepository(UserEntityRepository repository, User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Get all user entities from repository
        List<UserEntity> entities = repository.findAll();

        //Return all entities if user is admin
        if (user.isAdmin()) {
            return entities;
        }

        //Create result list
        List<UserEntity> resultList = new ArrayList<>();

        //Iterate over all entities in repository
        for (UserEntity entity : entities) {
            if (entity.isUserApproved(user)) {
                resultList.add(entity);
            }
        }

        //Return result
        return resultList;
    }
}