package org.citopt.sensmonqtt;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;

/**
 *
 * @author rafaelkperes
 */
@Configuration
public class MyRepositoryRestMvcConfiguration extends RepositoryRestMvcConfiguration {

    private static final String MY_BASE_PATH = "/api";

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        System.out.println("load RepositoryRestMvcConfiguration");
        super.configureRepositoryRestConfiguration(config);
        config.setBasePath(MY_BASE_PATH);
    }
}