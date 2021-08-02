package de.ipvs.as.mbp.domain.discovery.device.requirements.location;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.SimpleEntityResolver;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.device.location.LocationTemplate;
import de.ipvs.as.mbp.error.EntityValidationException;
import de.ipvs.as.mbp.repository.discovery.LocationTemplateRepository;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Objects of this class represent location requirements for devices.
 */
@JsonIgnoreProperties
public class LocationRequirement implements DeviceRequirement {
    //Type name of this requirement
    private static final String TYPE_NAME = "location";

    //Operator to use
    private LocationOperator operator;

    //ID of the location template to use
    private String locationTemplateId;

    /**
     * Creates a new location requirement.
     */
    public LocationRequirement() {

    }

    /**
     * Creates a new location requirement from a given location operator and a location template, given by its ID.
     *
     * @param operator           To operator to use
     * @param locationTemplateId The ID of the location template to use
     */
    @JsonCreator
    public LocationRequirement(@JsonProperty("operator") LocationOperator operator, @JsonProperty("locationTemplateId") String locationTemplateId) {
        setOperator(operator);
        setLocationTemplate(locationTemplateId);
    }

    /**
     * Returns the operator of the location requirement.
     *
     * @return The operator
     */
    public LocationOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator of the location requirement.
     *
     * @param operator The operator to set
     * @return The location requirement
     */
    public LocationRequirement setOperator(LocationOperator operator) {
        //Check compatibility between operator and template
        checkCompatibility(operator, resolveLocationTemplate(this.locationTemplateId));

        //Set operator
        this.operator = operator;
        return this;
    }

    /**
     * Returns the ID of the location template that is referenced in the location requirement.
     *
     * @return The location template ID
     */
    public String getLocationTemplateId() {
        return this.locationTemplateId;
    }

    /**
     * Returns the location template of the location requirement.
     *
     * @return The location template
     */
    @JsonIgnore
    public LocationTemplate getLocationTemplate() {
        return resolveLocationTemplate(this.locationTemplateId);
    }

    /**
     * Sets the location template of the location requirement.
     *
     * @param locationTemplate The location template to set
     * @return The location requirement
     */
    public LocationRequirement setLocationTemplate(LocationTemplate locationTemplate) {
        //Check compatibility between operator and template
        checkCompatibility(this.operator, locationTemplate);

        //Reference location template
        this.locationTemplateId = locationTemplate.getId();
        return this;
    }

    /**
     * Sets the location template of the location requirement by providing its ID.
     *
     * @param locationTemplateId The ID of the location template to set
     * @return The location requirement
     */
    public LocationRequirement setLocationTemplate(String locationTemplateId) {
        //Retrieve location template
        LocationTemplate locationTemplate = resolveLocationTemplate(locationTemplateId);

        //Sanity check
        if (locationTemplate == null) {
            return this;
        }

        //Check compatibility between operator and template
        checkCompatibility(this.operator, locationTemplate);

        //Reference location template
        this.locationTemplateId = locationTemplate.getId();
        return this;
    }

    /**
     * Checks whether a given location requirement operator is compatible to a given location template. If this is not
     * the case, an exception will be thrown. In case at least one of both objects is null, no exception is thrown.
     */
    public static void checkCompatibility(LocationOperator operator, LocationTemplate locationTemplate) {
        //Sanity check
        if ((operator == null) || (locationTemplate == null)) {
            return;
        }

        //Check compatibility
        if (!operator.getLocationTemplateTypes().contains(locationTemplate.getClass())) {
            throw new IllegalArgumentException("The operator is not compatible to the location template.");
        }
    }

    /**
     * Returns the type name of the requirement.
     *
     * @return The type name
     */
    @JsonProperty("type")
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Validates the device requirement by extending the provided exception with information about invalid fields.
     *
     * @param exception   The exception to extend as part of the validation
     * @param fieldPrefix Prefix that is supposed to be added to the fields that are validated
     */
    @Override
    public void validate(EntityValidationException exception, String fieldPrefix) {
        //Check operator
        if (operator == null) {
            exception.addInvalidField(fieldPrefix + ".operator", "An operator must be selected.");
        }

        //Check if location template exists
        if ((this.locationTemplateId == null) || (this.locationTemplateId.isEmpty()) || getLocationTemplate() == null) {
            exception.addInvalidField(fieldPrefix + ".locationTemplateId", "The referenced location template does not exist.");
        }
    }

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
    @Override
    public JSONObject toQueryRequirement(JSONObject jsonObject) throws JSONException {
        //Retrieve location template from repository
        LocationTemplate locationTemplate = getLocationTemplate();

        //Add operator
        jsonObject.put("operator", this.operator.value());

        //Get requirement details from the location template
        JSONObject requirementDetails = locationTemplate.toQueryRequirementDetails(new JSONObject());

        //Add details to JSONObject
        jsonObject.put("details", requirementDetails);

        //Return extended JSONObject
        return jsonObject;
    }

    /**
     * Resolves the referenced location template from the database.
     *
     * @param locationTemplateId The ID of the location template to resolve
     * @return The location template
     */
    private LocationTemplate resolveLocationTemplate(String locationTemplateId) {
        //Resolve location template
        Optional<Object> template = SimpleEntityResolver.resolve(LocationTemplateRepository.class, locationTemplateId);

        //Return template or null if not found
        return (LocationTemplate) template.orElse(null);
    }
}
