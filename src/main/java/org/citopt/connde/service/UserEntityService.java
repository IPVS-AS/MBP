package org.citopt.connde.service;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.domain.user_entity.UserEntityPolicy;
import org.citopt.connde.domain.user_entity.UserEntityRole;
import org.citopt.connde.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.citopt.connde.domain.user_entity.UserEntityRole.*;

/**
 * Service that supports managing of user entities.
 */
@Service
public class UserEntityService {

    private static final Sort DEFAULT_SORT = new Sort(Sort.Direction.ASC, "name");


    @Autowired
    private UserService userService;

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
        User user = userService.getUserWithAuthorities();

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
        UserEntity entity = (UserEntity) repository.findOne(entityId);

        //Check for null (not found)
        if (entity == null) {
            return null;
        }

        //Return entity if user is permitted
        if (entity.isReadable()) {
            return entity;
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
        User user = userService.getUserWithAuthorities();

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
    @SuppressWarnings("unchecked")
    public List<UserEntity> getUserEntitiesFromRepository(UserEntityRepository repository, User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Get all user entities from repository
        List<UserEntity> entities = repository.findAll(DEFAULT_SORT);

        //Create result list
        List<UserEntity> resultList = new ArrayList<>();

        //Iterate over all entities in repository
        for (UserEntity entity : entities) {
            //Check user permission
            if (entity.isReadable()) {
                resultList.add(entity);
            }
        }

        //Return result
        return resultList;
    }

    /**
     * Returns the set of user entity roles that the current user holds for a given user entity.
     *
     * @param entity The user entity to check for
     * @return The set of user entity roles
     */
    private Set<UserEntityRole> getUserEntityRoles(UserEntity entity) {
        //Create set for all user matching entity roles
        Set<UserEntityRole> roles = new HashSet<>();

        //Get current user
        User currentUser = userService.getUserWithAuthorities();

        //Check for anonymous
        if (currentUser == null) {
            roles.add(ANONYMOUS);
            return roles;
        }

        //User is registered
        roles.add(USER);

        //Check if user is approved
        if (entity.isUserApproved(currentUser)) {
            roles.add(APPROVED_USER);
        }

        //Check if user is owner
        if (entity.isUserOwner(currentUser)) {
            roles.add(ENTITY_OWNER);
        }

        //Check if user is admin
        if (currentUser.isAdmin()) {
            roles.add(ADMIN);
        }

        return roles;
    }

    /**
     * Checks if the current user has a certain permission regarding a given user entity.
     *
     * @param permission The permission to check
     * @param entity     The pertained user entity
     * @return True, if the user is permitted; false otherwise
     */
    public boolean isUserPermitted(UserEntity entity, String permission) {
        //Sanity checks
        if (entity == null) {
            throw new IllegalArgumentException("User entity must not be null.");
        } else if ((permission == null) || permission.isEmpty()) {
            throw new IllegalArgumentException("Permission must not be null or empty.");
        }

        //Get policy for this entity
        UserEntityPolicy policy = entity.getUserEntityPolicy();

        //Sanity check
        if (!policy.containsPermission(permission)) {
            throw new IllegalArgumentException("The permission is not part of the policy of this user entity.");
        }

        //Get roles of current user
        Set<UserEntityRole> userRoles = getUserEntityRoles(entity);

        //Check policy
        return policy.isPermitted(permission, userRoles);
    }
}