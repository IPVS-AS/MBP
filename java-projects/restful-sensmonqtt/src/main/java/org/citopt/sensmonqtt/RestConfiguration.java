package org.citopt.sensmonqtt;

import org.citopt.sensmonqtt.domain.component.Actuator;
import org.citopt.sensmonqtt.domain.component.ActuatorValidator;
import org.citopt.sensmonqtt.domain.component.Sensor;
import org.citopt.sensmonqtt.domain.component.SensorValidator;
import org.citopt.sensmonqtt.domain.device.Device;
import org.citopt.sensmonqtt.domain.device.DeviceValidator;
import org.citopt.sensmonqtt.domain.location.LocationValidator;
import org.citopt.sensmonqtt.domain.type.Type;
import org.citopt.sensmonqtt.web.rest.RestApiController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestMvcConfiguration;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

/**
 *
 * @author rafaelkperes
 */
@Configuration
public class RestConfiguration extends RepositoryRestMvcConfiguration {

    public static final String BASE_PATH = "/api";

    @Override
    protected void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        System.out.println("load RepositoryRestMvcConfiguration");
        super.configureRepositoryRestConfiguration(config);
        config.setBasePath(BASE_PATH);

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

    @Bean
    public ResourceProcessor<Resource<Sensor>> sensorProcessor() {

        return new ResourceProcessor<Resource<Sensor>>() {

            @Override
            public Resource<Sensor> process(Resource<Sensor> resource) {
                String id = resource.getContent().getId();
                Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.
                        methodOn(RestApiController.class).deploySensor(id))
                        .withRel("deploy");
                resource.add(link);
                return resource;
            }
        };
    }

    @Bean
    public ResourceProcessor<Resource<Actuator>> actuatorProcessor() {

        return new ResourceProcessor<Resource<Actuator>>() {

            @Override
            public Resource<Actuator> process(Resource<Actuator> resource) {
                String id = resource.getContent().getId();
                Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.
                        methodOn(RestApiController.class).deployActuator(id))
                        .withRel("deploy");
                resource.add(link);
                return resource;
            }
        };
    }
}
