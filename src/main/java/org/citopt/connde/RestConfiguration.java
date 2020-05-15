package org.citopt.connde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.AdapterValidator;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.ActuatorValidator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.device.DeviceValidator;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringAdapterValidator;
import org.citopt.connde.domain.rules.*;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.domain.testing.TestDetailsValidator;
import org.citopt.connde.domain.user.Authority;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.repository.AdapterRepository;
import org.citopt.connde.repository.SensorRepository;
import org.citopt.connde.service.UserService;
import org.citopt.connde.web.rest.RestDeploymentController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.ResourceProcessor;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Contains crucial rest configurations for the application.
 *
 * @author rafaelkperes, Jan
 */
@Configuration
public class RestConfiguration extends RepositoryRestConfigurerAdapter {
    //Base path for REST calls (URL prefix)
    public static final String BASE_PATH = "/api";

    /**
     * Creates a projection factory bean for applying projections on entities.
     *
     * @return The created bean
     */
    @Bean
    public SpelAwareProxyProjectionFactory projectionFactory() {
        return new SpelAwareProxyProjectionFactory();
    }

    /**
     * REST configuration for the repositories that are used within this application.
     *
     * @param config Repository REST configuration to extend
     */
    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        super.configureRepositoryRestConfiguration(config);

        System.out.println("load RepositoryRestMvcConfiguration");

        config.setBasePath(BASE_PATH);
        config.exposeIdsFor(Device.class, Adapter.class, MonitoringAdapter.class, Actuator.class, Sensor.class,
                User.class, Authority.class, ComponentType.class, Rule.class, RuleTrigger.class, RuleAction.class, TestDetails.class);
    }

    /**
     * Creates and adds validators for the REST documents.
     *
     * @param v Validating repository event listener to extend
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {

        //Adapters
        v.addValidator("beforeSave", new AdapterValidator());
        v.addValidator("beforeCreate", new AdapterValidator());

        //Monitoring adapters
        v.addValidator("beforeSave", new MonitoringAdapterValidator());
        v.addValidator("beforeCreate", new MonitoringAdapterValidator());

        //Sensors
        v.addValidator("beforeSave", new SensorValidator());
        v.addValidator("beforeCreate", new SensorValidator());

        //Actuators
        v.addValidator("beforeSave", new ActuatorValidator());
        v.addValidator("beforeCreate", new ActuatorValidator());

        //Devices
        v.addValidator("beforeSave", new DeviceValidator());
        v.addValidator("beforeCreate", new DeviceValidator());

        //Rules
        v.addValidator("beforeSave", new RuleValidator());
        v.addValidator("beforeCreate", new RuleValidator());

        //Rule actions
        v.addValidator("beforeSave", new RuleActionValidator());
        v.addValidator("beforeCreate", new RuleActionValidator());

        //Rule triggers
        v.addValidator("beforeSave", new RuleTriggerValidator());
        v.addValidator("beforeCreate", new RuleTriggerValidator());

        //TestDetails
        v.addValidator("beforeSave", new TestDetailsValidator());
        v.addValidator("beforeCreate", new TestDetailsValidator());
    }

    /**
     * Resource processor for sensors.
     *
     * @return The resource processor
     */
    @Bean
    public ResourceProcessor<Resource<Sensor>> sensorProcessor() {

        return new ResourceProcessor<Resource<Sensor>>() {

            /**
             * Processing method for sensor resources.
             * @param resource The sensor resource to process
             * @return The processed sensor resource
             */
            @Override
            public Resource<Sensor> process(Resource<Sensor> resource) {
                //Get sensor id
                String id = resource.getContent().getId();
                //Link sensor with deployment
                Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.
                        methodOn(RestDeploymentController.class).deploySensor(id))
                        .withRel("deploy");
                resource.add(link);
                return resource;
            }
        };
    }

    /**
     * Resource processor for actuators.
     *
     * @return The resource processor
     */
    @Bean
    public ResourceProcessor<Resource<Actuator>> actuatorProcessor() {

        return new ResourceProcessor<Resource<Actuator>>() {

            /**
             * Processing method for actuator resources.
             * @param resource The actuator resource to process
             * @return The processed actuator resource
             */
            @Override
            public Resource<Actuator> process(Resource<Actuator> resource) {
                //Get actuator id
                String id = resource.getContent().getId();
                //Link actuator with deployment
                Link link = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.
                        methodOn(RestDeploymentController.class).deployActuator(id))
                        .withRel("deploy");
                resource.add(link);
                return resource;
            }
        };
    }
}
