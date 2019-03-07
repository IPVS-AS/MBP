package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.componentType.ComponentType;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 *
 */

public class MonitoringAdapter extends Adapter{
    private List<ComponentType> deviceTypes;

    public List<ComponentType> getDeviceTypes() {
        return deviceTypes;
    }

    public void setDeviceTypes(List<ComponentType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }
}
