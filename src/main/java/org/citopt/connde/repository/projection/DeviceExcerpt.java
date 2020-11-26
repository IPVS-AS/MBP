package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.device.Device;
import org.springframework.data.rest.core.config.Projection;

@Projection(name = "list", types = Device.class)
public interface DeviceExcerpt extends UserEntityExcerpt {

    String getId();

    String getName();

    String getComponentType();

    String getMacAddress();

    String getIpAddress();

    String getDate();

    String getUsername();
}
