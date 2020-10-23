package org.citopt.connde;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.adapter.AdapterValidator;
import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.ActuatorValidator;
import org.citopt.connde.domain.component.Sensor;
import org.citopt.connde.domain.component.SensorValidator;
import org.citopt.connde.domain.device.Device;
import org.citopt.connde.domain.device.DeviceValidator;
import org.citopt.connde.domain.entity_type.*;
import org.citopt.connde.domain.env_model.EnvironmentModel;
import org.citopt.connde.domain.key_pair.KeyPair;
import org.citopt.connde.domain.key_pair.KeyPairValidator;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.citopt.connde.domain.monitoring.MonitoringAdapterValidator;
import org.citopt.connde.domain.rules.Rule;
import org.citopt.connde.domain.rules.RuleAction;
import org.citopt.connde.domain.rules.RuleActionValidator;
import org.citopt.connde.domain.rules.RuleTrigger;
import org.citopt.connde.domain.rules.RuleTriggerValidator;
import org.citopt.connde.domain.rules.RuleValidator;
import org.citopt.connde.domain.testing.TestDetails;
import org.citopt.connde.domain.testing.TestDetailsValidator;
import org.citopt.connde.domain.user.Authority;
import org.citopt.connde.domain.user.User;
import org.citopt.connde.error.EntityNotFoundException;
import org.citopt.connde.error.MissingPermissionException;
import org.citopt.connde.web.rest.RestDeploymentController;
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
                Adapter.class, MonitoringAdapter.class,
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
