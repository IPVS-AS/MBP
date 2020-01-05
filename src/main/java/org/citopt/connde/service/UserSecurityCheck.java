package org.citopt.connde.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.domain.user_entity.UserEntity;
import org.citopt.connde.repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserSecurityCheck {

    @Autowired
    private UserService userService;

    @Autowired
    private UserEntityService userEntityService;

    public boolean check(UserEntity entity, String permission) {

        User user = userService.getUserWithAuthorities();

        System.out.println("hier");
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean checkPage(PageImpl<UserEntity> page, Pageable pageable, UserEntityRepository repository) {
        //Get all device user entities the current user has access to
        List<UserEntity> userEntities = userEntityService.getUserEntitiesFromRepository(repository);

        List content = null;

        //Try to extract the content of the passed page using reflection
        try {
            content = (List) FieldUtils.readField(page, "content", true);
        } catch (IllegalAccessException ignored) {
        }

        //Sanity check
        if (content == null) {
            return false;
        }

        //Calculate start and end of page from pageable
        int start = pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), userEntities.size());

        //Replace elements of content list with the new page elements
        content.clear();
        content.addAll(userEntities.subList(start, end));

        //Update total counter using reflection
        try {
            FieldUtils.writeField(page, "total", content.size(), true);
        } catch (IllegalAccessException ignored) {
        }

        return true;
    }
}
