package org.citopt.connde;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.ActuatorValidator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.device.DeviceValidator;
import org.citopt.connde.web.rest.RestDeploymentController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

/**
 * @author Jan
 */
@Configuration
public class RestConfiguration extends RepositoryRestConfigurerAdapter {

    public static final String BASE_PATH = "/api";

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        super.configureRepositoryRestConfiguration(config);
        System.out.println("load RepositoryRestMvcConfiguration");
        config.setBasePath(BASE_PATH);
        config.exposeIdsFor(Device.class, Adapter.class, Actuator.class, Sensor.class);
    }

    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {
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
                        methodOn(RestDeploymentController.class).deploySensor(id))
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
                        methodOn(RestDeploymentController.class).deployActuator(id))
                        .withRel("deploy");
                resource.add(link);
                return resource;
            }
        };
    }
}
