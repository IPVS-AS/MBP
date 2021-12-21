package de.ipvs.as.mbp;

import de.ipvs.as.mbp.security.UserSessionCookieFilter;
import de.ipvs.as.mbp.service.user.UserSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    //URLs to important pages
    public static final String URL_LOGIN = "/login";
    public static final String URL_LOGOUT = "/logout";

    //Cookie filter for validating session cookies
    private final UserSessionCookieFilter userSessionCookieFilter;

    @Autowired
    public SecurityConfiguration(UserSessionService userSessionService) {
        this.userSessionCookieFilter = new UserSessionCookieFilter(userSessionService);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/resources/**")
                .antMatchers("/webapp/**")
                .antMatchers(URL_LOGIN, "/templates/register")
                .antMatchers(HttpMethod.GET, RestConfiguration.BASE_PATH + "/settings/mbpinfo")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/users")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/checkOauthTokenUser")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/checkOauthTokenSuperuser")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/checkOauthTokenAcl");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //Configure HTTP security
        http
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .sessionFixation()
                    .migrateSession().and()
                .csrf(AbstractHttpConfigurer::disable)
                .logout(c -> {
                    c.logoutRequestMatcher(new AntPathRequestMatcher(URL_LOGOUT));
                    c.deleteCookies(UserSessionCookieFilter.SESSION_COOKIE_NAME);
                    c.logoutSuccessHandler(userSessionCookieFilter);
                })
                .authorizeRequests(c -> {
                    c.antMatchers("/api/users/login").permitAll();
                    c.antMatchers("/api/**").authenticated();
                })
                .addFilterAfter(userSessionCookieFilter, SecurityContextPersistenceFilter.class)
                .exceptionHandling(c -> c
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
    }
}
