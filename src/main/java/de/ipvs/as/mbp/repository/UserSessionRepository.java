package de.ipvs.as.mbp.repository;

import de.ipvs.as.mbp.domain.user.UserSession;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * MongoDB repository for user sessions.
 */
@Repository
public interface UserSessionRepository extends MongoRepository<UserSession, String> {

    /**
     * Returns whether an user session with a given session ID exists in the repository.
     *
     * @param sessionId The session ID to check
     * @return True, if an user session with the given ID exists; false otherwise
     */
    boolean existsBySessionId(@Param("sessionId") String sessionId);

    /**
     * Returns whether an user session with a given user ID exists in the repository.
     *
     * @param userId The user ID to check
     * @return True, if an user session with the given ID exists; false otherwise
     */
    boolean existsByUserId(@Param("userId") String userId);

    /**
     * Returns the first user session from the repository which has a given session ID.
     *
     * @param sessionId The session ID to search for
     * @return Optional containing the first matching user session (if existing)
     */
    Optional<UserSession> findFirstBySessionId(@Param("sessionId") String sessionId);

    /**
     * Returns the first user session from the repository which has a given user ID.
     *
     * @param userId The user ID to search for
     * @return Optional containing the first matching user session (if existing)
     */
    Optional<UserSession> findFirstByUserId(@Param("userId") String userId);
}
