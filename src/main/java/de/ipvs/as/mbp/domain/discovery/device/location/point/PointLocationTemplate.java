package de.ipvs.as.mbp.domain.discovery.device.location.point;

import de.ipvs.as.mbp.domain.discovery.device.location.LocationTemplate;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Objects of this class represent location templates for location points.
 */
@MBPEntity(createValidator = PointLocationTemplateCreateValidator.class)
public class PointLocationTemplate extends LocationTemplate {
    private double latitude;
    private double longitude;

    public PointLocationTemplate() {

    }

    public double getLatitude() {
        return latitude;
    }

    public PointLocationTemplate setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public PointLocationTemplate setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    /**
     * Transforms the location template to a {@link JSONObject} that can be used as part of a location requirement
     * within a device description query in order to provide more details about the requirement. The transformation
     * happens by extending a provided {@link JSONObject} for necessary fields and finally returning the extended
     * {@link JSONObject} again. The type of the location template does not need to be explicitly added.
     *
     * @param jsonObject The {@link JSONObject} to extend
     * @return The resulting extended {@link JSONObject}
     * @throws JSONException In case a non-resolvable issue occurred during the transformation
     */
    @Override
    public JSONObject toQueryRequirementDetails(JSONObject jsonObject) throws JSONException {
        //Add fields
        jsonObject.put("lat", this.latitude);
        jsonObject.put("lon", this.longitude);

        //Return extended JSONObject
        return jsonObject;
    }
}
