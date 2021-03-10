package de.ipvs.as.mbp.domain.monitoring;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;

/**
 * Objects of this class represent components that consist out of a device and a
 * monitoring adapter that can be used for monitoring of devices.
 */
public class MonitoringComponent extends Component {

    private static final String COMPONENT_TYPE_NAME = "monitoring";

    /**
     * Creates a new, empty monitoring component.
     */
    public MonitoringComponent() {

    }

    /**
     * Creates a new monitoring component, consisting out of a monitoring adapter
     * and a compatible device.
     *
     * @param monitoringOperator The monitoring adapter to use
     * @param device            The corresponding device
     */
    public MonitoringComponent(MonitoringOperator monitoringOperator, Device device) {
        // Create id
        String adapterId = monitoringOperator.getId();
        String deviceId = device.getId();
        setId(adapterId + "@" + deviceId);

        // Set name
        setName(monitoringOperator.getName() + " (" + device.getName() + ")");

        // Use given adapter and device
        setOperator(monitoringOperator);
        setDevice(device);
    }

    /**
     * Creates a monitoring component from a given full component ID, including the ID of the monitoring operator and
     * the ID of the corresponding device.
     *
     * @param componentID The full component ID
     */
    public MonitoringComponent(String componentID){
        setId(componentID);
    }

    /**
     * Returns the monitoring operator ID of the monitoring component.
     *
     * @return The ID of the monitoring operator
     */
    public String getMonitoringOperatorID(){
        //Split ID
        String[] splits = getId().split("@");

        //Check splits for validity
        if(splits.length != 2){
            throw new IllegalStateException("The ID of the monitoring component appears to be illegal.");
        }

        //Return monitoring operator ID
        return splits[0];
    }

    /**
     * Returns the device ID of the monitoring component.
     *
     * @return The ID of the device
     */
    public String getDeviceID(){
        //Split ID
        String[] splits = getId().split("@");

        //Check splits for validity
        if(splits.length != 2){
            throw new IllegalStateException("The ID of the monitoring component appears to be illegal.");
        }

        //Return device ID
        return splits[1];
    }

    @Override
    public String getComponentTypeName() {
        return COMPONENT_TYPE_NAME;
    }

}
