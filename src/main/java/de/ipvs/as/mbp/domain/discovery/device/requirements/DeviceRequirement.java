package de.ipvs.as.mbp.domain.discovery.device.requirements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ipvs.as.mbp.error.EntityValidationException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Abstract base class for device requirements.
 */
public abstract class DeviceRequirement {
    /**
     * Creates a new device requirement.
     */
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

    /**
     * Transforms the device requirement to a {@link JSONObject} that can be used as requirement within a device
     * description query. The transformation happens by extending a provided {@link JSONObject} for necessary fields
     * and finally returning the extended {@link JSONObject} again.
     * The type of the requirement does not need to be explicitly added.
     *
     * @param jsonObject The {@link JSONObject} to extend
     * @return The resulting extended {@link JSONObject}
     * @throws JSONException In case a non-resolvable issue occurred during the transformation
     */
    @JsonIgnore
    public abstract JSONObject toQueryRequirement(JSONObject jsonObject) throws JSONException;
}
