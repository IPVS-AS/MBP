package de.ipvs.as.mbp;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class PropertySourcesConfiguration {

    /**
     * Create a configurer for the injection of application-related property values, based on the
     * application.properties file.
     *
     * @return The resulting property configurer
     */
    @Bean(name="applicationPropertiesConfigurer")
    public PropertySourcesPlaceholderConfigurer applicationPropertiesConfigurer() {
        //Create corresponding property configurer and make it tolerant
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("application.properties"));
        propsConfig.setIgnoreUnresolvablePlaceholders(true);

        //Return final configurer
        return propsConfig;
    }

    /**
     * Create a configurer for the injection of git-related property values, based on the git.properties file.
     *
     * @return The resulting property configurer
     */
    @Bean(name="gitPropertiesConfigurer")
    public PropertySourcesPlaceholderConfigurer gitPropertiesConfigurer() {
        //Create corresponding property configurer and make it tolerant
        PropertySourcesPlaceholderConfigurer propsConfig = new PropertySourcesPlaceholderConfigurer();
        propsConfig.setLocation(new ClassPathResource("git.properties"));
        propsConfig.setIgnoreResourceNotFound(true);
        propsConfig.setIgnoreUnresolvablePlaceholders(true);

        //Return final configurer
        return propsConfig;
    }
}
