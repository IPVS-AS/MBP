package de.ipvs.as.mbp.repository.projection;

import de.ipvs.as.mbp.domain.entity_type.DeviceType;
import de.ipvs.as.mbp.domain.monitoring.MonitoringOperator;
import org.springframework.data.rest.core.config.Projection;

import java.util.List;

/**
 * Projection for monitoring adapters which is suitable for lists.
 */
@Projection(name = "list", types = MonitoringOperator.class)
public interface MonitoringOperatorExcerpt extends OperatorExcerpt {
    List<DeviceType> getDeviceTypes();
}
