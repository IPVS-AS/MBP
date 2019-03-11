package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.componentType.ComponentType;
import org.citopt.connde.domain.device.Device;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

/**
 *
 */

public class MonitoringAdapter extends Adapter{
    @DBRef
    private List<ComponentType> deviceTypes;

    public List<ComponentType> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<ComponentType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
