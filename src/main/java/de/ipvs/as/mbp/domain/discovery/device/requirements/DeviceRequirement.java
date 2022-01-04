package de.ipvs.as.mbp.domain.discovery.device.requirements;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.ipvs.as.mbp.error.EntityValidationException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Generic interface for device requirements.
 */
public interface DeviceRequirement {
    /**
     * Returns the type name of the requirement.
     *
     * @return The type name
     */
    String getTypeName();

    /**
     * Validates the device requirement by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    void validate(EntityValidationException exception, String fieldPrefix);

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
    JSONObject toQueryRequirement(JSONObject jsonObject) throws JSONException;
}
