package org.citopt.connde;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration that enables the automatic storage of auditing data (created date, modified date, creator, ...)
 * for Mongo DB repositories.
 */
@Configuration
@EnableMongoAuditing(auditorAwareRef = "auditorProvider")
public class MongoAuditingConfiguration {
    /**
     * Returns the auditor, i.e. the name of the currently active user for the purpose of storing auditing data
     * in the MongoDB repositories.
     *
     * @return The name of the currently active user
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
