package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.entity_type.DeviceType;
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
