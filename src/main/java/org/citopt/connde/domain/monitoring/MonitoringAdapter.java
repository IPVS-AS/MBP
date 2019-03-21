package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.adapter.Adapter;
import org.citopt.connde.domain.componentType.ComponentType;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.List;

/**
 * Similar to pure adapters, monitoring adapters wrap scripts that can be deployed on devices with the purpose
 * of monitoring the device infrastructure. In contrast to ordinary adapters, monitoring adapters are
 * associated with certain device types and can only be deployed on devices of the same type.
 */
public class MonitoringAdapter extends Adapter {
    @DBRef
    private List<ComponentType> deviceTypes;

    /**
     * Returns the list of device types that are associated with this adapter.
     *
     * @return The list of device types
     */
    public List<ComponentType> getDeviceTypes() {
        return deviceTypes;
    }

    /**
     * Sets the list of device types that are associated with this adapter.
     *
     * @return The list of device types to set
     */
    public void setDeviceTypes(List<ComponentType> deviceTypes) {
        this.deviceTypes = deviceTypes;
    }

    /**
     * Returns the number of device types that are associated with this adapter.
     *
     * @return The number of device types
     */
    public int getDeviceTypesNumber() {
        //Check if list has been created
        if (deviceTypes == null) {
            return 0;
        }
        return deviceTypes.size();
    }
}
