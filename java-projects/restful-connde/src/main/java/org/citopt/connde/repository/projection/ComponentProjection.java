package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.component.Actuator;
import org.citopt.connde.domain.component.Sensor;
import org.springframework.data.rest.core.config.Projection;

/**
 *
 * @author rafaelkperes
 */
@Projection(name = "list", types = {Actuator.class, Sensor.class})
public interface ComponentProjection {

    String getId();

    String getName();

}
