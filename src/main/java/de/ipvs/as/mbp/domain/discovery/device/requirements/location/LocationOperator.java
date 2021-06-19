package de.ipvs.as.mbp.domain.discovery.device.requirements.location;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.ipvs.as.mbp.domain.discovery.device.requirements.DeviceRequirementOperator;
import de.ipvs.as.mbp.domain.discovery.location.LocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.circle.CircleLocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.informal.InformalLocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.point.PointLocationTemplate;
import de.ipvs.as.mbp.domain.discovery.location.polygon.PolygonLocationTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Enumeration of operators that can be used in location requirements.
 */
public enum LocationOperator implements DeviceRequirementOperator {
    DESCRIBED_BY("described_by", Collections.singletonList(InformalLocationTemplate.class)),
    AT_LOCATION("at_location", Collections.singletonList(PointLocationTemplate.class)),
    IN_AREA("in_area", Arrays.asList(CircleLocationTemplate.class, PolygonLocationTemplate.class));

    //Externally visible name of the operator
    private String name;

    //Location template types on which the operator applies
    private List<Class<? extends LocationTemplate>> locationTemplateTypes;

    /**
     * Creates a new location requirement operator with a given name and corresponding location template types.
     *
     * @param name                  The desired name of the operator
     * @param locationTemplateTypes The matching location template types
     */
    LocationOperator(String name, List<Class<? extends LocationTemplate>> locationTemplateTypes) {
        setName(name);
        setLocationTemplateTypes(locationTemplateTypes);
    }

    /**
     * Sets the name of the location requirement operator.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the location template types that match the location requirement operator.
     *
     * @param locationTemplateTypes The location template types to set
     */
    private void setLocationTemplateTypes(List<Class<? extends LocationTemplate>> locationTemplateTypes) {
        this.locationTemplateTypes = locationTemplateTypes;
    }

    /**
     * Returns the location template types that match the location requirement operator.
     *
     * @return The location template types
     */
    public List<Class<? extends LocationTemplate>> getLocationTemplateTypes() {
        return this.locationTemplateTypes;
    }

    /**
     * Serializes a location requirement operator by returning its name.
     *
     * @return The name of the operator
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the location requirement operator that corresponds to a given name. This method is called when
     * a provided operator name needs to be mapped to the actual operator object.
     *
     * @param name The name of the location requirement operator
     * @return The corresponding location requirement operator or null if not found
     */
    @JsonCreator
    public static LocationOperator create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available operator against the provided name
        for (LocationOperator operator : values()) {
            if (name.equalsIgnoreCase(operator.value())) {
                //Matching operator found
                return operator;
            }
        }

        //No matching operator was found
        return null;
    }
}
