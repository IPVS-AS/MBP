package org.citopt.connde.web.rest.event_handler;

import org.citopt.connde.domain.user_entity.UserEntity;
import org.springframework.data.rest.core.annotation.HandleAfterCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

@Component
@RepositoryEventHandler
public class UserEntityEventHandler {
    @HandleAfterCreate
    public void afterUserEntityCreate(UserEntity userEntity) {
        //TODO set owner here
        System.out.println("user entity created");
    }
}
