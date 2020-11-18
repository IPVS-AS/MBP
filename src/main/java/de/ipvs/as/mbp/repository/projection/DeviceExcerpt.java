package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.device.Device;
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
