package org.citopt.connde;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration that enables the automatic storage of auditing data (created date, modified date, creator, ...)
 * for Mongo DB repositories.
 */
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
public class MongoAuditingConfiguration {
    //Default name to use for auditing in case there is no active authentication
    private static final String DEFAULT_AUDITING_NAME = "System";

    /**
     * Returns the auditor, i.e. the name of the currently active user for the purpose of storing auditing data
     * in the repositories. In case there is no active authentication, the default name is returned.
     *
     * @return The name of the currently active user
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        //Get security context
        SecurityContext context = SecurityContextHolder.getContext();

        //Get current user authentication
        Authentication authentication = context.getAuthentication();

        //Check if the authentication exists
        if(authentication == null){
            //No authentication, return default name
            return () -> DEFAULT_AUDITING_NAME;
        }

        //Return the user name of the current authentication
        return () -> authentication.getName();
    }
}
