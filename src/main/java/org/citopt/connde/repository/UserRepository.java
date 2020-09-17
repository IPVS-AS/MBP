package org.citopt.connde.repository;

import java.util.List;
import java.util.Optional;

import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.projection.UserExcerpt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Spring Data MongoDB repository for the User entity.
 */
//@RepositoryRestResource(exported = false, excerptProjection = UserExcerpt.class) //Annotation could be removed
public interface UserRepository extends MongoRepository<User, String> {
	
	@Query(value = "{ username : :#{#username} }", exists = true)
	boolean existsByUsername(@Param("username") String username);
	
	@Query(value = "{ $and : [ { 'id' : { $ne : :#{#id} } }, { username : :#{#username} } ] }", exists = true)
	boolean existsOtherByUsername(@Param("id") String id, @Param("username") String username);
	
	@Query("{ username : :#{#username} }")
    Optional<User> findByUsername(@Param("username") String username);

    List<UserExcerpt> findByUsernameContains(String username);
}
