package de.ipvs.as.mbp.domain.discovery.device.location.informal;

import de.ipvs.as.mbp.domain.discovery.device.location.LocationTemplate;
import de.ipvs.as.mbp.domain.discovery.device.operators.StringOperator;
import de.ipvs.as.mbp.domain.user_entity.MBPEntity;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Objects of this class represent location templates for informal location descriptions.
 */
@MBPEntity(createValidator = InformalLocationTemplateCreateValidator.class)
public class InformalLocationTemplate extends LocationTemplate {
    private StringOperator operator;
    private String match;

    public InformalLocationTemplate() {
        super();
    }

    public StringOperator getOperator() {
        return operator;
    }

    public InformalLocationTemplate setOperator(StringOperator operator) {
        this.operator = operator;
        return this;
    }

    public String getMatch() {
        return match;
    }

    public InformalLocationTemplate setMatch(String match) {
        this.match = match;
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
        jsonObject.put("operator", this.operator.value());
        jsonObject.put("match", this.match);

        //Return extended JSONObject
        return jsonObject;
    }
}
