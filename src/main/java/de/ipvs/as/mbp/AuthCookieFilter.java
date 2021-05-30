package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.repository.UserSessionRepository;
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


public class AuthCookieFilter extends GenericFilterBean implements LogoutSuccessHandler {

    public final static String COOKIE_NAME = "authentication";

    private UserSessionRepository userSessionRepository;

    public AuthCookieFilter(UserSessionRepository userSessionRepository) {
        this.userSessionRepository = userSessionRepository;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String sessionId = extractAuthenticationCookie(httpServletRequest);

        if (sessionId != null) {
            User user = new User();
            user.setAdmin(true);
            user.setUsername("admin");
            user.setFirstName("Test");
            user.setLastName("User");
            UserAuthentication userAuthentication = new UserAuthentication(user);
            SecurityContextHolder.getContext().setAuthentication(userAuthentication);
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    public static String extractAuthenticationCookie(HttpServletRequest httpServletRequest) {
        String sessionId = null;
        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(AuthCookieFilter.COOKIE_NAME)) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        return sessionId;
    }

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        System.out.println("asdf");

        String sessionId = extractAuthenticationCookie(request);
        //TODO delete session

        response.sendRedirect(request.getContextPath() + SecurityConfiguration.URL_LOGIN);
    }
}