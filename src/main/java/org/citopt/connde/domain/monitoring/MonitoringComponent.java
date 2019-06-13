package org.citopt.connde.domain.monitoring;

import org.citopt.connde.domain.component.Component;
import org.citopt.connde.domain.device.Device;

/**
 * Objects of this class represent components that consist out of a device and a monitoring adapter that can be used
 * for monitoring of devices.
 */
public class MonitoringComponent extends Component {
    private static final String COMPONENT_TYPE_NAME = "monitoring";

    /**
     * Creates a new monitoring component, consisting out of a monitoring adapter and a compatible device.
     *
     * @param monitoringAdapter The monitoring adapter to use
     * @param device            The corresponding device
     */
    public MonitoringComponent(MonitoringAdapter monitoringAdapter, Device device) {
        //Create id
        String adapterId = monitoringAdapter.getId();
        String deviceId = device.getId();
        setId(adapterId + "@" + deviceId);

        //Set name
        setName(monitoringAdapter.getName() + " (" + device.getName() + ")");

        //Use given adapter and device
        setAdapter(monitoringAdapter);
        setDevice(device);
    }

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }
}
