package de.ipvs.as.mbp;

import de.ipvs.as.mbp.security.RestAuthenticationEntryPoint;
import de.ipvs.as.mbp.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * Security configuration
 *
 * @author Imeri Amil
 */

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
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
            auth
                    .userDetailsService(userDetailsService)
                    .passwordEncoder(passwordEncoder());
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
                .csrf().disable()
                .httpBasic().authenticationEntryPoint(restAuthenticationEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/authenticate").permitAll()
                .antMatchers("/api/**").authenticated()
                .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login")
                .invalidateHttpSession(true).deleteCookies("JSESSIONID");
    }
}
