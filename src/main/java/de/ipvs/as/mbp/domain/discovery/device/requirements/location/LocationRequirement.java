package de.ipvs.as.mbp.domain.discovery.device.requirements.location;

import de.ipvs.as.mbp.domain.SimpleEntityResolver;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirement;
import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.repository.discovery.LocationTemplateRepository;

import java.util.Optional;

/**
 * Objects of this class represent location requirements for devices.
 */
public class LocationRequirement extends DeviceRequirement {
    //Type name of this requirement
    private static final String TYPE_NAME = "location";

    private LocationRequirementOperator operator;
    private String locationTemplateId;

    /**
     * Creates a new location requirement.
     */
    public LocationRequirement() {

    }

    /**
     * Returns the operator of the location requirement.
     *
     * @return The operator
     */
    public LocationRequirementOperator getOperator() {
        return operator;
    }

    /**
     * Sets the operator of the location requirement.
     *
     * @param operator The operator to set
     * @return The location requirement
     */
    public LocationRequirement setOperator(LocationRequirementOperator operator) {
        //Check compatibility between operator and template
        checkCompatibility(operator, resolveLocationTemplate());

        //Set operator
        this.operator = operator;
        return this;
    }

    /**
     * Returns the location template of the location requirement.
     *
     * @return The location template
     */
    public LocationTemplate getLocationTemplate() {
        return resolveLocationTemplate();
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
     * Checks whether a given location requirement operator is compatible to a given location template. If this is not
     * the case, an exception will be thrown. In case at least one of both objects is null, no exception is thrown.
     */
    public static void checkCompatibility(LocationRequirementOperator operator, LocationTemplate locationTemplate) {
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
     * Returns the name of the requirement.
     *
     * @return The name
     */
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Resolves the referenced location template from the database.
     *
     * @return The location template
     */
    private LocationTemplate resolveLocationTemplate() {
        //Resolve location template
        Optional<Object> template = SimpleEntityResolver.resolve(LocationTemplateRepository.class, this.locationTemplateId);

        //Return template or null of not found
        return (LocationTemplate) template.orElse(null);
    }
}
