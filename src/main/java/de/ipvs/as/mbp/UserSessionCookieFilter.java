package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.service.user.UserSessionService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * Objects of this class behaves as filter for user session cookies by exploiting functions offered by
 * a user session service that allows to manage user sessions.
 */
public class UserSessionCookieFilter extends GenericFilterBean implements LogoutSuccessHandler {

    //Name to use for the session cookie
    public final static String SESSION_COOKIE_NAME = "user_session";

    private UserSessionService userSessionService;

    /**
     * Creates a new filter for user session cookies by passing a reference to a service that allows the
     * management of user sessions.
     *
     * @param userSessionService The user session service to use
     */
    public UserSessionCookieFilter(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    /**
     * Applies the cookie filter to incoming HTTP requests by checking whether a session ID is provided as cookie
     * and looking it up into the user session repository. If it exists, a authorization is set corresponding
     * to the user to which the session belongs.
     *
     * @param servletRequest  The incoming HTTP request
     * @param servletResponse The intended response to the HTTP request
     * @param filterChain     The current filter chain
     * @throws IOException      In case of an I/O error
     * @throws ServletException In case of an servlet error
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws ServletException, IOException {

        //Get session of the request and try to find the associated user
        Optional<User> userOptional = getUserFromRequest((HttpServletRequest) servletRequest);

        //Check if user could be found
        if (userOptional.isPresent()) {
            //Create and set corresponding authentication object
            UserAuthentication userAuthentication = new UserAuthentication(userOptional.get());
            SecurityContextHolder.getContext().setAuthentication(userAuthentication);
        }

        //Execute remainder of filter chain
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        //Retrieve session ID from request cookie
        String sessionId = getSessionIdFromRequest(request);

        //Invalidate session
        userSessionService.invalidateSession(sessionId);

        //Reply with redirect to login page
        response.sendRedirect(request.getContextPath() + SecurityConfiguration.URL_LOGIN);
    }

    /**
     * Extracts the session ID from the corresponding cookie of a given HttpServletRequest and looks up the
     * user that is associated with this session.
     *
     * @param request The HttpServletRequest to extract the user for
     * @return Optional containing the associated user (if existing)
     */
    private Optional<User> getUserFromRequest(HttpServletRequest request) {
        //Extract session ID (if existing) from HTTP request
        String sessionId = getSessionIdFromRequest(request);

        //Check if session ID could be found
        if ((sessionId == null) || sessionId.isEmpty()) {
            return Optional.empty();
        }

        //Try to find user that is associated with the session (if exists)
        return userSessionService.getUserBySessionId(sessionId);
    }

    /**
     * Extracts and returns the session ID from the corresponding cookie of a given HttpServletRequest.
     *
     * @param request The HttpServletRequest to extract the session ID for
     * @return The extracted session ID or null in case the cookie is not present
     */
    private String getSessionIdFromRequest(HttpServletRequest request) {
        //Retrieve all cookies from the request
        Cookie[] cookies = request.getCookies();

        //Check if cookies are available
        if (cookies == null) {
            return null;
        }

        //Iterate over all cookies
        for (Cookie cookie : cookies) {
            //Check for matching cookie name
            if (cookie.getName().equals(UserSessionCookieFilter.SESSION_COOKIE_NAME)) {
                //Cookie name matches, thus return its value
                return cookie.getValue();
            }
        }

        //No matching cookie available
        return null;
    }
}