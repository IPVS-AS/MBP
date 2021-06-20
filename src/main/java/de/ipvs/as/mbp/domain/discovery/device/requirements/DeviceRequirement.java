package de.ipvs.as.mbp.domain.discovery.device.requirements;

import de.ipvs.as.mbp.error.EntityValidationException;

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

    /**
     * Validates the device requirement by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    public abstract void validate(EntityValidationException exception, String fieldPrefix);
}
