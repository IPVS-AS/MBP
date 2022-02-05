package de.ipvs.as.mbp.domain.discovery.description;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of available attachment types.
 */
public enum DeviceDescriptionAttachmentType {
    ACTUATOR("actuator"), SENSOR("sensor");

    //Externally visible name of the attachment type
    private String name;

    /**
     * Creates a new attachment type with a given name.
     *
     * @param name The desired name of the attachment type
     */
    DeviceDescriptionAttachmentType(String name) {
        setName(name);
    }

    /**
     * Sets the name of the attachment type.
     *
     * @param name The name to set
     */
    private void setName(String name) {
        this.name = name;
    }

    /**
     * Serializes the attachment type by returning its name.
     *
     * @return The name of the attachment type
     */
    @JsonValue
    public String value() {
        return this.name;
    }

    /**
     * Returns the attachment type that corresponds to a given name. This method is called when
     * a provided type name needs to be mapped to the actual attachment type object.
     *
     * @param name The name of the attachment type
     * @return The corresponding attachment type or null if not found
     */
    @JsonCreator
    public static DeviceDescriptionAttachmentType create(String name) {
        //Sanity check for provided name
        if ((name == null) || name.isEmpty()) {
            return null;
        }

        //Compare every available attachment type against the provided name
        for (DeviceDescriptionAttachmentType type : values()) {
            if (name.equalsIgnoreCase(type.value())) {
                //Matching attachment type found
                return type;
            }
        }

        //No matching attachment type was found
        return null;
    }
}