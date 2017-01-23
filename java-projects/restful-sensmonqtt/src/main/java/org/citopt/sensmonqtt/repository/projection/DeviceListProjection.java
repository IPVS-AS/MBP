package org.citopt.sensmonqtt.repository.projection;

import org.citopt.sensmonqtt.domain.device.Device;
import org.springframework.data.rest.core.config.Projection;

/**
 *
 * @author rafaelkperes
 */
@Projection(name = "list", types = Device.class)
public interface DeviceListProjection {

    String getId();

    String getName();

    String getMacAddress();
}
