package org.citopt.connde.domain.entity_type;

import org.citopt.connde.domain.device.Device;

public class DeviceType extends EntityType {

    private boolean sshSupport = false;

    public DeviceType(){
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
}
