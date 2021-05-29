package de.ipvs.as.mbp;

import de.ipvs.as.mbp.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.BeanInitializationException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HeaderWriterLogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.security.web.header.writers.ClearSiteDataHeaderWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AuthCookieFilter authCookieFilter;

    private final CustomLogoutSuccessHandler logoutSuccessHandler;

    public SecurityConfiguration() {
        this.authCookieFilter = new AuthCookieFilter();
        this.logoutSuccessHandler = new CustomLogoutSuccessHandler();
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
                .antMatchers("/login", "/templates/register")
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
                    c.addLogoutHandler(new HeaderWriterLogoutHandler(
                            new ClearSiteDataHeaderWriter(ClearSiteDataHeaderWriter.Directive.ALL)));
                    c.logoutSuccessHandler(this.logoutSuccessHandler);
                    c.deleteCookies(AuthCookieFilter.COOKIE_NAME);
                })
                .authorizeRequests(c -> {
                    c.antMatchers("/api/authenticate").permitAll();
                    c.antMatchers("/api/**").authenticated();
                })
                .exceptionHandling(c -> c
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterAfter(this.authCookieFilter, SecurityContextPersistenceFilter.class);

        //.logout()
        //.logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
    }

    private static class CustomLogoutSuccessHandler implements LogoutSuccessHandler {

        public CustomLogoutSuccessHandler() {
        }

        @Override
        public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
                                    Authentication authentication) {

            /*
            String sessionId = AuthCookieFilter.extractAuthenticationCookie(request);
            if (sessionId != null) {
                this.dsl.delete(APP_SESSION).where(APP_SESSION.ID.eq(sessionId)).execute();
            }

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().flush();*/
        }

    }
}
