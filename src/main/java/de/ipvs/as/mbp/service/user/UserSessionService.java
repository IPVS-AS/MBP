package de.ipvs.as.mbp.service.user;

import de.ipvs.as.mbp.AuthCookieFilter;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.domain.user.UserSession;
import de.ipvs.as.mbp.repository.UserRepository;
import de.ipvs.as.mbp.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Service class for managing user sessions.
 */
@Service
public class UserSessionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    public ResponseCookie createSessionCookie(User user) {
        //Create new session for given user
        UserSession userSession = createSession(user);

        //Create corresponding cookie from session
        return ResponseCookie
                .from(AuthCookieFilter.COOKIE_NAME, userSession.getSessionId())
                .maxAge(Duration.ofDays(30)).sameSite("strict").httpOnly(true).secure(true)
                .path("/").build();
    }

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