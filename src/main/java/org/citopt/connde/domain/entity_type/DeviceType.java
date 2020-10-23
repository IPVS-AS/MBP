package org.citopt.connde.domain.entity_type;

import org.citopt.connde.domain.device.Device;

public class DeviceType extends EntityType {

    private boolean sshSupport = false;

    public DeviceType() {
        super();
    }

    public DeviceType(String name) {
        super(name);
    }

    public DeviceType(String name, boolean sshSupport) {
        super(name);
        this.sshSupport = sshSupport;
    }

    public boolean isSSHSupport() {
        return sshSupport;
    }

    public void setSSHSupport(boolean sshSupport) {
        this.sshSupport = sshSupport;
    }

    /**
     * Returns the class of the user entity for which this type is for.
     *
     * @return The class of the user entity
     */
    @Override
    public Class<Device> getEntityClass() {
        return Device.class;
    }

    /**
     * Creates and returns a new device type by a given name, SSH support flag and an icon context.
     *
     * @param name        The name of the new device type
     * @param sshSupport  True, if devices of the new type support SSH; false otherwise
     * @param iconContent An URL to an existing icon or a base64 string of an icon to use for this device entity
     * @return The created device type
     */
    public static DeviceType createDeviceType(String name, boolean sshSupport, String iconContent) {
        //Create device type
        DeviceType deviceType = new DeviceType(name, sshSupport);

        //Set icon
        deviceType.setIcon(new EntityTypeIcon(iconContent));
        return deviceType;
    }
}
