package org.citopt.connde;

import org.citopt.connde.constants.Constants;
import org.citopt.connde.security.RestAuthenticationEntryPoint;
import org.citopt.connde.service.UserDetailsServiceImpl;
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
		.antMatchers(HttpMethod.POST,"/api/authenticate")
		.antMatchers(HttpMethod.POST,"/api/users")
	    .antMatchers(HttpMethod.POST,"/api/checkOauthTokenUser")
	    .antMatchers(HttpMethod.POST,"/api/checkOauthTokenSuperuser")
        .antMatchers(HttpMethod.POST,"/api/checkOauthTokenAcl");
	}

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
        	.csrf().disable()
				.httpBasic()
				.authenticationEntryPoint(restAuthenticationEntryPoint())
			.and()
				.authorizeRequests()
	            .antMatchers("/api/**").authenticated()
            .and()
	            .logout()
	            .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
	            .logoutSuccessUrl("/login")
	            .invalidateHttpSession(true)
	            .deleteCookies("JSESSIONID");
    }
}
