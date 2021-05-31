package de.ipvs.as.mbp.service.user;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.ipvs.as.mbp.UserSessionCookieFilter;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user.UserSession;
import de.ipvs.as.mbp.repository.UserRepository;
import de.ipvs.as.mbp.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Service class for managing user sessions.
 */
@Service
public class UserSessionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    //Cache users that are associated with session IDs
    private final Cache<String, User> sessionCache;

    /**
     * Creates the user session service.
     */
    public UserSessionService() {
        //Create and configure cache
        this.sessionCache = Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(1000).build();
    }

    /**
     * Returns the user that is associated with a session, given by its session ID.
     *
     * @param sessionId The ID of the session to retrieve the user for
     * @return Optional containing the user that is associated with the session (if existing)
     */
    public Optional<User> getUserBySessionId(String sessionId) {
        //Lookup user from cache
        User cachedUser = this.sessionCache.getIfPresent(sessionId);

        //Check whether the cache entry existed
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        }

        //User is not cached, retrieve corresponding session from repository
        Optional<UserSession> userSessionOptional = userSessionRepository.findFirstBySessionId(sessionId);

        //Check if session exists
        if ((!userSessionOptional.isPresent())) {
            return Optional.empty();
        }

        //Get user ID from session
        String userId = userSessionOptional.get().getUserId();

        //Check if user ID is available
        if ((userId == null) || userId.isEmpty()) {
            return Optional.empty();
        }

        //Retrieve corresponding user by its ID
        Optional<User> userOptional = userRepository.findById(userId);

        //Update cache if user was found
        userOptional.ifPresent(user -> sessionCache.put(sessionId, user));

        //Return user optional
        return userOptional;
    }

    /**
     * Invalidates a session by removing it from the repository.
     *
     * @param sessionId The ID of the session to invalidate
     */
    public void invalidateSession(String sessionId) {
        //Delete session from repository
        userSessionRepository.deleteById(sessionId);

        //Invalidate entry in cache
        sessionCache.invalidate(sessionId);
    }

    /**
     * Creates and saves a new session for a given user and returns a cookie representing
     * this session by its session ID.
     *
     * @param user The user to create the session for
     * @return The resulting cookie containing the session ID
     */
    public ResponseCookie createSessionCookie(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Create new session for given user
        UserSession userSession = createSession(user);

        //Create corresponding cookie from session
        return ResponseCookie
                .from(UserSessionCookieFilter.SESSION_COOKIE_NAME, userSession.getSessionId())
                .maxAge(Duration.ofDays(30)).sameSite("strict").httpOnly(true).secure(true)
                .path("/").build();
    }

    /**
     * Creates and stores a new session for a given user.
     *
     * @param user The user to create the session for
     * @return THe created and stored session
     */
    private UserSession createSession(User user) {
        //Sanity check
        if (user == null) {
            throw new IllegalArgumentException("User must not be null.");
        }

        //Create new session
        UserSession userSession = new UserSession(user);

        //Store session in repository
        return userSessionRepository.insert(userSession);
    }
}