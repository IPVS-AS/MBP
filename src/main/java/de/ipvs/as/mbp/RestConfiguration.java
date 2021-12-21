package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.entity_type.ActuatorType;
import de.ipvs.as.mbp.domain.entity_type.DeviceType;
import de.ipvs.as.mbp.domain.entity_type.EntityType;
import de.ipvs.as.mbp.domain.entity_type.SensorType;
import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.rules.Rule;
import de.ipvs.as.mbp.domain.rules.RuleAction;
import de.ipvs.as.mbp.domain.rules.RuleTrigger;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.web.rest.RestDeploymentController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Contains crucial rest configurations for the application.
 */
@Configuration
public class RestConfiguration implements RepositoryRestConfigurer {
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
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        RepositoryRestConfigurer.super.configureRepositoryRestConfiguration(config, cors);
        System.out.println("load RepositoryRestMvcConfiguration");

        //Set base path
        config.setBasePath(BASE_PATH);

        //Include entity IDs of the following entity types into REST request responses
        config.exposeIdsFor(KeyPair.class,
                Device.class,
                Operator.class, MonitoringOperator.class,
                Actuator.class, Sensor.class,
                User.class,
                EntityType.class,
                EnvironmentModel.class,
                Rule.class, RuleTrigger.class, RuleAction.class,
                DeviceType.class, ActuatorType.class, SensorType.class,
                TestDetails.class);
    }

    /**
     * Resource processor for sensors.
     *
     * @return The resource processor
     */
    @Bean
    public RepresentationModelProcessor<EntityModel<Sensor>> sensorProcessor() {
        return new RepresentationModelProcessor<EntityModel<Sensor>>() {
            /**
             * Processing method for sensor resources.
             * @param resource The sensor resource to process
             * @return The processed sensor resource
             */
            @Override
            public @NonNull EntityModel<Sensor> process(@NonNull EntityModel<Sensor> resource) {
                //Get sensor id
                String id = resource.getContent().getId();
                //Link sensor with deployment
                Link link = null;
                try {
                    link = linkTo(WebMvcLinkBuilder.methodOn(RestDeploymentController.class).deploySensor(null, id)).withRel("deploy");
                } catch (EntityNotFoundException | MissingPermissionException e) {
                    // Does not matter here since only link building
                    e.printStackTrace();
                }
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
    public RepresentationModelProcessor<EntityModel<Actuator>> actuatorProcessor() {
        return new RepresentationModelProcessor<EntityModel<Actuator>>() {
            /**
             * Processing method for actuator resources.
             * @param resource The actuator resource to process
             * @return The processed actuator resource
             */
            @Override
            public @NonNull EntityModel<Actuator> process(@NonNull EntityModel<Actuator> resource) {
                //Get actuator id
                String id = resource.getContent().getId();
                //Link actuator with deployment
                Link link = null;
                try {
                    link = linkTo(WebMvcLinkBuilder.methodOn(RestDeploymentController.class).deployActuator(null, id)).withRel("deploy");
                } catch (EntityNotFoundException | MissingPermissionException e) {
                    // Does not matter here since only link building
                    e.printStackTrace();
                }
                resource.add(link);
                return resource;
            }
        };
    }

}
