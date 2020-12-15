package org.citopt.connde.repository;

import java.util.List;
import java.util.Optional;

import org.citopt.connde.domain.gamification.GamificationUser;
import org.citopt.connde.repository.projection.ComponentExcerpt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RestResource;

/**
 * Spring Data MongoDB repository for the GamificationUser entity.
 */
public interface GamificationUserRepository extends MongoRepository<GamificationUser, String> {
	
	@RestResource(exported = false)
    @Query("{'_id': ?0}")
	GamificationUser get(String id);
	
	@RestResource(exported = false)
	@Query(value="{ 'user.id' : ?0 }")
    Optional<GamificationUser> findByUserid(String userID);
}
