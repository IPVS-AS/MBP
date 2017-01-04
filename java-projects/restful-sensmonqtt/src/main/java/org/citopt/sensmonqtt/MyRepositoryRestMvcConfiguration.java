package org.citopt.sensmonqtt;

import org.citopt.sensmonqtt.domain.component.Actuator;
import org.citopt.sensmonqtt.domain.component.ActuatorValidator;
import org.citopt.sensmonqtt.domain.component.Sensor;
import org.citopt.sensmonqtt.domain.component.SensorValidator;
import org.citopt.sensmonqtt.domain.device.Device;
import org.citopt.sensmonqtt.domain.device.DeviceValidator;
import org.citopt.sensmonqtt.domain.location.LocationValidator;
import org.citopt.sensmonqtt.domain.type.Type;
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
        
        config.exposeIdsFor(Device.class, Type.class, Actuator.class, Sensor.class);
    }

    @Override
    protected void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
        v.addValidator("beforeSave", new LocationValidator());
        v.addValidator("beforeCreate", new LocationValidator());
        
        v.addValidator("beforeSave", new SensorValidator());
        v.addValidator("beforeCreate", new SensorValidator());
        
        v.addValidator("beforeSave", new ActuatorValidator());
        v.addValidator("beforeCreate", new ActuatorValidator());
        
        v.addValidator("beforeSave", new DeviceValidator());
        v.addValidator("beforeCreate", new DeviceValidator());
    }
}
