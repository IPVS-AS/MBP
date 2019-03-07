package org.citopt.connde.repository.projection;

import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.domain.monitoring.MonitoringAdapter;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

@Projection(name = "list", types = MonitoringAdapter.class)
public interface MonitoringAdapterListProjection extends AdapterListProjection {
    List<ComponentType> getDeviceTypes();
}
