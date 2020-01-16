package org.citopt.connde;

import org.citopt.connde.constants.Constants;
import org.citopt.connde.security.RestAuthenticationEntryPoint;
import org.citopt.connde.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
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
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public RestAuthenticationEntryPoint restAuthenticationEntryPoint() {
        return new RestAuthenticationEntryPoint();
    }

    @Bean
    public UserDetailsServiceImpl mongoUserDetails() {
        return new UserDetailsServiceImpl();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(mongoUserDetails()).passwordEncoder(passwordEncoder());
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers(HttpMethod.OPTIONS, "/**")
                .antMatchers("/resources/**")
                .antMatchers("/webapp/**");
    }

    @Override
    @Order(Ordered.HIGHEST_PRECEDENCE)
    protected void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/api/authenticate").permitAll()
                .antMatchers(HttpMethod.POST, "/api/users").hasAuthority(Constants.ADMIN)
                .antMatchers(HttpMethod.PUT, "/api/users").hasAuthority(Constants.ADMIN)
                .antMatchers(HttpMethod.GET, "/api/users").hasAuthority(Constants.ADMIN)
                .antMatchers(HttpMethod.GET, "/api/users/:username").hasAuthority(Constants.ADMIN)
                .antMatchers(HttpMethod.DELETE, "/api/users/:username").hasAuthority(Constants.ADMIN)
                .antMatchers("/oauth/**").authenticated()
            .and().httpBasic().authenticationEntryPoint(restAuthenticationEntryPoint())
            .and()
                .logout()
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID");

        //http.addFilterAfter(new CustomFilter(), BasicAuthenticationFilter.class);
    }
}
