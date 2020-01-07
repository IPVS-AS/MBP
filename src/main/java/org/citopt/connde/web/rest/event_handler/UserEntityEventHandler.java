package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

/**
 * Event handlers for all user entities.
 */
@Component
@RepositoryEventHandler
public class UserEntityEventHandler {

    @Autowired
    private UserService userService;

    /**
     * Called in case an user entity is supposed to be created. This method then takes care of setting
     * the owner of the user entity to the current user, before it is stored in its dedicated repository.
     *
     * @param userEntity The user entity that is supposed to be created
     */
    @HandleBeforeCreate
    public void afterUserEntityCreate(UserEntity userEntity) {
        //Sanity check
        if (userEntity == null) {
            throw new IllegalArgumentException("User entity must not be null.");
        }

        //Get current user
        User currentUser = userService.getUserWithAuthorities();

        //Set owner
        userEntity.setOwner(currentUser);
    }
}
