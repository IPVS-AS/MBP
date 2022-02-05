package de.ipvs.as.mbp.domain.discovery.description;

/**
 * Objects of this class represent descriptions of the objects that are either affected or observed by
 * {@link DeviceDescriptionAttachment}s (actuators or sensors) within {@link DeviceDescription}s.
 */
public class DeviceDescriptionAttachmentObject {
    //Textual description of the object
    private String description;

    //Physical quantity that is either affected or observed
    private String quantity;

    /**
     * Creates a new empty attachment object.
     */
    public DeviceDescriptionAttachmentObject() {

    }

    /**
     * Returns the description of the attachment object.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the attachment object.
     *
     * @param description The description to set
     * @return The attachment object
     */
    public DeviceDescriptionAttachmentObject setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns the physical quantity that is either affected or observed by the attachment.
     *
     * @return The physical quantity
     */
    public String getQuantity() {
        return quantity;
    }

    /**
     * Sets the physical quantity that is either affected or observed by the attachment.
     *
     * @param quantity The physical quantity to set
     * @return The attachment object
     */
    public DeviceDescriptionAttachmentObject setQuantity(String quantity) {
        this.quantity = quantity;
        return this;
    }
}
