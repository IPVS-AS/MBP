package de.ipvs.as.mbp.domain.discovery.device.requirements;

/**
 * Abstract base class for device requirements.
 */
public abstract class DeviceRequirement {
    public DeviceRequirement() {

    }

    /**
     * Returns the name of the requirement.
     *
     * @return The name
     */
    public abstract String getTypeName();
}
