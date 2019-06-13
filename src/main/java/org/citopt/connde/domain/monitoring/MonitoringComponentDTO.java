package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;

public class MonitoringComponentDTO {
    private String id;
    private String name;
    private String monitoringAdapterId;
    private String deviceId;

    public MonitoringComponentDTO(MonitoringComponent monitoringComponent) {
        this.id = monitoringComponent.getId();
        this.name = monitoringComponent.getName();
        this.monitoringAdapterId = monitoringComponent.getAdapter().getId();
        this.deviceId = monitoringComponent.getDevice().getId();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMonitoringAdapterId() {
        return monitoringAdapterId;
    }

    public String getDeviceId() {
        return deviceId;
    }
}
