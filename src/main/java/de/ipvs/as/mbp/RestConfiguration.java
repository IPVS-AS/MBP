package de.ipvs.as.mbp;

import de.ipvs.as.mbp.domain.component.Actuator;
import de.ipvs.as.mbp.domain.component.ActuatorValidator;
import de.ipvs.as.mbp.domain.component.Sensor;
import de.ipvs.as.mbp.domain.component.SensorValidator;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.device.DeviceValidator;
import de.ipvs.as.mbp.domain.entity_type.*;
import de.ipvs.as.mbp.domain.env_model.EnvironmentModel;
import de.ipvs.as.mbp.domain.key_pair.KeyPair;
import de.ipvs.as.mbp.domain.key_pair.KeyPairValidator;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperatorValidator;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.domain.operator.OperatorValidator;
import de.ipvs.as.mbp.domain.rules.*;
import de.ipvs.as.mbp.domain.testing.TestDetails;
import de.ipvs.as.mbp.domain.testing.TestDetailsValidator;
import de.ipvs.as.mbp.domain.user.Authority;
import de.ipvs.as.mbp.domain.user.User;
import de.ipvs.as.mbp.error.EntityNotFoundException;
import de.ipvs.as.mbp.error.MissingPermissionException;
import de.ipvs.as.mbp.web.rest.RestDeploymentController;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.event.ValidatingRepositoryEventListener;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

/**
 * Contains crucial rest configurations for the application.
 *
 * @author rafaelkperes, Jan
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
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        System.out.println("load RepositoryRestMvcConfiguration");

        //Set base path
        config.setBasePath(BASE_PATH);

        //Include entity IDs of the following entity types into REST request responses
        config.exposeIdsFor(KeyPair.class,
                Device.class,
                Operator.class, MonitoringOperator.class,
                Actuator.class, Sensor.class,
                User.class, Authority.class,
                EntityType.class,
                EnvironmentModel.class,
                Rule.class, RuleTrigger.class, RuleAction.class,
                DeviceType.class, ActuatorType.class, SensorType.class,
                TestDetails.class);
    }

    /**
     * Creates and adds validators for the REST documents.
     *
     * @param v Validating repository event listener to extend
     */
    @Override
    public void configureValidatingRepositoryEventListener(ValidatingRepositoryEventListener v) {

        //Adapters
        v.addValidator("beforeSave", new OperatorValidator());
        v.addValidator("beforeCreate", new OperatorValidator());

        //Monitoring adapters
        v.addValidator("beforeSave", new MonitoringOperatorValidator());
        v.addValidator("beforeCreate", new MonitoringOperatorValidator());

        //Sensors
        v.addValidator("beforeSave", new SensorValidator());
        v.addValidator("beforeCreate", new SensorValidator());

        //Actuators
        v.addValidator("beforeSave", new ActuatorValidator());
        v.addValidator("beforeCreate", new ActuatorValidator());

        //Key pairs
        v.addValidator("beforeSave", new KeyPairValidator());
        v.addValidator("beforeCreate", new KeyPairValidator());

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

        //Entity types
        v.addValidator("beforeSave", new EntityTypeValidator());
        v.addValidator("beforeCreate", new EntityTypeValidator());

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
    public RepresentationModelProcessor<EntityModel<Sensor>> sensorProcessor() {
        return new RepresentationModelProcessor<EntityModel<Sensor>>() {
            /**
             * Processing method for sensor resources.
             * @param resource The sensor resource to process
             * @return The processed sensor resource
             */
            @Override
            public EntityModel<Sensor> process(EntityModel<Sensor> resource) {
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
            public EntityModel<Actuator> process(EntityModel<Actuator> resource) {
                //Get actuator id
                String id = resource.getContent().getId();
                //Link actuator with deployment
                Link link = null;
                try {
                    link = linkTo(WebMvcLinkBuilder.methodOn(RestDeploymentController.class).deployActuator(id, null)).withRel("deploy");
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
