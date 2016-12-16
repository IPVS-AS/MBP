package org.citopt.sensmonqtt;

import org.citopt.sensmonqtt.domain.component.ComponentValidator;
import org.citopt.sensmonqtt.domain.device.DeviceValidator;
import org.citopt.sensmonqtt.domain.location.LocationValidator;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
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

    @Override
    protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        v.addValidator("beforeSave", new LocationValidator());
        v.addValidator("beforeCreate", new LocationValidator());
        
        v.addValidator("beforeSave", new ComponentValidator());
        v.addValidator("beforeCreate", new ComponentValidator());
        
        v.addValidator("beforeSave", new DeviceValidator());
        v.addValidator("beforeCreate", new DeviceValidator());
    }
}
