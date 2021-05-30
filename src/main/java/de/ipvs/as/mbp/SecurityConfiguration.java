package de.ipvs.as.mbp;

import de.ipvs.as.mbp.repository.UserSessionRepository;
import de.ipvs.as.mbp.service.user.UserDetailsServiceImpl;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
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

    private final AuthCookieFilter authCookieFilter;

    @Autowired
    public SecurityConfiguration(UserSessionRepository userSessionRepository) {
        this.authCookieFilter = new AuthCookieFilter(userSessionRepository);
    }

    @Bean
    public UserDetailsService mongoUserDetails() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) {
        try {
            UserDetailsService userDetailsService = mongoUserDetails();
            auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        } catch (Exception e) {
            throw new BeanInitializationException("Security configuration failed", e);
        }
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/resources/**")
                .antMatchers("/webapp/**")
                .antMatchers(URL_LOGIN, "/templates/register")
                .antMatchers(HttpMethod.GET, RestConfiguration.BASE_PATH + "/settings/mbpinfo")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/users/authenticate")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/users")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/checkOauthTokenUser")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/checkOauthTokenSuperuser")
                .antMatchers(HttpMethod.POST, RestConfiguration.BASE_PATH + "/checkOauthTokenAcl");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .logout(c -> {
                    c.logoutRequestMatcher(new AntPathRequestMatcher(URL_LOGOUT));
                    c.deleteCookies(AuthCookieFilter.COOKIE_NAME);
                    c.logoutSuccessHandler(authCookieFilter);
                })
                .authorizeRequests(c -> {
                    c.antMatchers("/api/authenticate").permitAll();
                    c.antMatchers("/api/**").authenticated();
                })
                .exceptionHandling(c -> c
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterAfter(this.authCookieFilter, SecurityContextPersistenceFilter.class);
    }
}
