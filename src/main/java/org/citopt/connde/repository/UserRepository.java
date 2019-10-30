package org.citopt.connde.repository;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.projection.UserExcerpt;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for the User entity.
 *
 * @author Imeri Amil
 */
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findOneByUsername(String username);

    List<UserExcerpt> findByUsernameContains(String username);
}
