package de.ipvs.as.mbp.domain.discovery.peripheral;

/**
 * Collection of details about a device on which a {@link DynamicPeripheral} is or may be deployed.
 */
public class DynamicPeripheralDeviceDetails {
    //MAC address of the device
    private String macAddress;

    //IP address of the device
    private String ipAddress;

    //Username for SSH
    private String username;


}
