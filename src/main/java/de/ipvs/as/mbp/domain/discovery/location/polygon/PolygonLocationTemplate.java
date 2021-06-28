package de.ipvs.as.mbp.domain.discovery.location.polygon;

import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Objects of this class represent location templates for polygon areas.
 */
@MBPEntity(createValidator = PolygonLocationTemplateCreateValidator.class)
public class PolygonLocationTemplate extends LocationTemplate {
    private List<List<Double>> points;

    public PolygonLocationTemplate() {

    }

    public List<List<Double>> getPoints() {
        return points;
    }

    public PolygonLocationTemplate setPoints(List<List<Double>> points) {
        this.points = points;
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
        //Create JSONArray for points
        JSONArray pointsArray = new JSONArray();

        //Stream through all points and add them to the array
        this.points.stream().map(JSONArray::new).forEach(pointsArray::put);

        //Add points array to JSONObject
        jsonObject.put("polygon", pointsArray);

        //Return extended JSONObject
        return jsonObject;
    }
}
