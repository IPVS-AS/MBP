package org.citopt.connde.repository;

import java.util.Optional;

import org.citopt.connde.domain.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
* Spring Data MongoDB repository for the User entity.
* @author Imeri Amil
*/
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findOneByUsername(String username);
}
