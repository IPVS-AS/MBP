package org.citopt.sensmonqtt.repository.projection;

import org.citopt.sensmonqtt.domain.type.Type;
import org.springframework.data.rest.core.config.Projection;

/**
 *
 * @author rafaelkperes
 */
@Projection(name = "list", types = Type.class)
public interface TypeListProjection {

    String getId();

    String getName();

    String getDescription();
}
