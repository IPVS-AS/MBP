package de.ipvs.as.mbp.domain.discovery.location.circle;

import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Objects of this class represent location templates for circle areas.
 */
@MBPEntity(createValidator = CircleLocationTemplateCreateValidator.class)
public class CircleLocationTemplate extends LocationTemplate {
    private double latitude;
    private double longitude;
    private double radius;

    public CircleLocationTemplate() {

    }

    public double getLatitude() {
        return latitude;
    }

    public CircleLocationTemplate setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getLongitude() {
        return longitude;
    }

    public CircleLocationTemplate setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getRadius() {
        return radius;
    }

    public CircleLocationTemplate setRadius(double radius) {
        this.radius = radius;
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
        jsonObject.put("radius", this.radius);

        //Return extended JSONObject
        return jsonObject;
    }
}
