package org.citopt.connde.repository;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.projection.UserExcerpt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the User entity.
 */
@RepositoryRestResource(exported = false, excerptProjection = UserExcerpt.class) //Annotation could be removed
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findOneByUsername(String username);

    List<UserExcerpt> findByUsernameContains(String username);
}
