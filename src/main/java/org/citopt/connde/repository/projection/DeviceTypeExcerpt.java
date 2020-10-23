package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.entity_type.DeviceType;
import org.citopt.connde.domain.entity_type.EntityType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Basic list excerpt for device types.
 */
@Projection(name = "list", types = {DeviceType.class})
public interface DeviceTypeExcerpt extends EntityTypeExcerpt {

    String getId();

    String getName();

    boolean getSSHSupport();
}
