package de.ipvs.as.mbp.domain.discovery.description;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objects of this class represent descriptions of device attachments within {@link DeviceDescription}s,
 * i.e. actuators and sensors that are connected with the device.
 */
public class DeviceDescriptionAttachment {

    //Type of the attachment
    @JsonProperty("type")
    private DeviceDescriptionAttachmentType attachmentType;

    //Name of the attachment
    private String name;

    //Model name of the attachment
    @JsonProperty("model")
    private String modelName;

    //Object that is observed/affected by the attachment
    private DeviceDescriptionAttachmentObject object;

    //Port through which the attachment is connected to the device
    @JsonProperty("port")
    private int portNumber;

    /**
     * Creates a new, empty attachment of a given type.
     *
     * @param attachmentType The attachment type to set
     */
    @JsonCreator
    public DeviceDescriptionAttachment(DeviceDescriptionAttachmentType attachmentType) {
        setAttachmentType(attachmentType);
    }

    /**
     * Returns the type of the attachment.
     *
     * @return The attachment type
     */
    public DeviceDescriptionAttachmentType getAttachmentType() {
        return attachmentType;
    }

    /**
     * Sets the type of the attachment.
     *
     * @param attachmentType The attachment type to set
     * @return The attachment
     */
    public DeviceDescriptionAttachment setAttachmentType(DeviceDescriptionAttachmentType attachmentType) {
        //Sanity check
        if (attachmentType == null) {
            throw new IllegalArgumentException("The attachment type must not be null.");
        }

        //Set field
        this.attachmentType = attachmentType;
        return this;
    }

    /**
     * Returns the name of the attachment.
     *
     * @return The attachment name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the attachment.
     *
     * @param name The attachment name to set
     * @return The attachment
     */
    public DeviceDescriptionAttachment setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the model name of the attachment.
     *
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the model name of the attachment.
     *
     * @param modelName The model name to set
     * @return The attachment
     */
    public DeviceDescriptionAttachment setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Returns the description of the object that is either observed or affected by the attachment.
     *
     * @return The object description
     */
    public DeviceDescriptionAttachmentObject getObject() {
        return object;
    }

    /**
     * Sets the description of the object that is either observed or affected by the attachment.
     *
     * @param object The object description to set
     * @return The attachment
     */
    public DeviceDescriptionAttachment setObject(DeviceDescriptionAttachmentObject object) {
        this.object = object;
        return this;
    }

    /**
     * Returns the number of the port through which the attachment is connected to the device.
     *
     * @return The port number
     */
    public int getPortNumber() {
        return portNumber;
    }

    /**
     * Sets the number of the port through which the attachment is connected to the device
     *
     * @param port The port number
     * @return The attachment
     */
    public DeviceDescriptionAttachment setPortNumber(int port) {
        this.portNumber = port;
        return this;
    }
}
