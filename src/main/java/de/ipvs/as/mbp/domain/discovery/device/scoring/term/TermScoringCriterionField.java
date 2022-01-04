package de.ipvs.as.mbp.domain.discovery.device.scoring.term;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;

import java.util.function.Function;

/**
 * Enumeration of device description fields that can be addressed within {@link TermScoringCriterion}s. Each field
 * consists out of a name and a retrieval function that can be used in order to retrieve the field value as string from
 * a given {@link DeviceDescription}.
 */
public enum TermScoringCriterionField {
    NAME("name", DeviceDescription::getName),
    DESCRIPTION("description", DeviceDescription::getDescription),
    TYPE("type", deviceDescription -> deviceDescription.getIdentifiers() == null ? null : deviceDescription.getIdentifiers().getType()),
    MODEL("type", deviceDescription -> deviceDescription.getIdentifiers() == null ? null : deviceDescription.getIdentifiers().getModelName()),
    MANUFACTURER("type", deviceDescription -> deviceDescription.getIdentifiers() == null ? null : deviceDescription.getIdentifiers().getManufacturer());

    //Name of the field
    private String name;
    //Takes a device description and retrieves the corresponding field value from it
    private Function<DeviceDescription, String> retrievalFunction;

    /**
     * Creates a new field name from a given name and a retrieval function that can be used in order to retrieve
     * the field value as string from a given {@link DeviceDescription}.
     *
     * @param name              The name of the field to use
     * @param retrievalFunction The retrieval function to use
     */
    TermScoringCriterionField(String name, Function<DeviceDescription, String> retrievalFunction) {
        //Set fields
        setName(name);
        setRetrievalFunction(retrievalFunction);
    }

    /**
     * Uses the retrieval function in order to retrieve the value of the field from a given {@link DeviceDescription}
     * as string.
     *
     * @param deviceDescription The device description for which the field is supposed to be retrieved
     * @return The retrieved field as string
     */
    public String retrieveField(DeviceDescription deviceDescription) {
        //Apply the retrieval function
        return this.retrievalFunction.apply(deviceDescription);
    }


    /**
     * Sets the name of the field.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        //Sanity check
        if ((name == null) || (name.isEmpty())) {
            throw new IllegalArgumentException("The name must not be null or empty.");
        }
        this.name = name;
    }

    /**
     * Serializes a device description field by returning its name.
     *
     * @return The name of the field
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Sets the retrieval function that retrieves the field value as string from a given {@link DeviceDescription}.
     *
     * @param retrievalFunction The retrieval function to set
     */
    private void setRetrievalFunction(Function<DeviceDescription, String> retrievalFunction) {
        //Null check
        if (retrievalFunction == null) {
            throw new IllegalArgumentException("The retrieval function must not be null.");
        }
        this.retrievalFunction = retrievalFunction;
    }

    /**
     * Returns the retrieval function that retrieves the field value as string from a given {@link DeviceDescription}.
     * {@link DeviceDescription}.
     *
     * @return retrievalFunction The retrieval function
     */
    public Function<DeviceDescription, String> getRetrievalFunction() {
        return this.retrievalFunction;
    }

    /**
     * Returns the device description field that corresponds to a given name. This method is called when
     * a provided field name needs to be mapped to the actual field object.
     *
     * @param name The name of the device description field
     * @return The corresponding field object or null if not found
     */
    @JsonCreator
    public static TermScoringCriterionField create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available field against the provided name
        for (TermScoringCriterionField field : values()) {
            if (name.equalsIgnoreCase(field.value())) {
                //Matching field found
                return field;
            }
        }

        //No matching field was found
        return null;
    }
}
