package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.user.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


public class AuthCookieFilter extends GenericFilterBean {

    public final static String COOKIE_NAME = "authentication";

    public AuthCookieFilter() {
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
}