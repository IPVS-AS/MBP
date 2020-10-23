package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.entity_type.DeviceType;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

/**
 * Projection for monitoring adapters which is suitable for lists.
 */
@Projection(name = "list", types = MonitoringAdapter.class)
public interface MonitoringAdapterExcerpt extends AdapterExcerpt {
    List<DeviceType> getDeviceTypes();
}
