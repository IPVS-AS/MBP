package de.ipvs.as.mbp.domain.discovery.description;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.ipvs.as.mbp.domain.discovery.description.deserializer.DeviceDescriptionCapabilitiesDeserializer;
import de.ipvs.as.mbp.util.InstantFromEpochMilliDeserializer;
import de.ipvs.as.mbp.util.InstantToStringSerializer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Objects of this class represent descriptions of devices and thus model the properties and capabilities of them.
 */
public class DeviceDescription {

    //User-defined Name of the device
    private String name;

    //Description of the device
    private String description;

    //Keywords
    private Set<String> keywords;

    //Location of the device description
    private DeviceDescriptionLocation location;

    //Device identifiers
    private DeviceDescriptionIdentifiers identifiers;

    //Device capabilities
    @JsonDeserialize(using = DeviceDescriptionCapabilitiesDeserializer.class)
    private List<DeviceDescriptionCapability> capabilities;

    //Device attachments (actuators and sensors)
    private List<DeviceDescriptionAttachment> attachments;

    //SSH details
    @JsonProperty("ssh")
    private DeviceDescriptionSSHDetails sshDetails;

    //Timestamp indicating the last update of the description
    @JsonProperty("last_update")
    @JsonDeserialize(using = InstantFromEpochMilliDeserializer.class)
    @JsonSerialize(using = InstantToStringSerializer.class)
    private Instant lastUpdateTimestamp;

    /**
     * Creates a new, empty device description.
     */
    public DeviceDescription() {
        //Initialize lists
        this.capabilities = new ArrayList<>();
        this.attachments = new ArrayList<>();
    }

    /**
     * Returns the name of the device description.
     *
     * @return The device name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the device description.
     *
     * @param name The name to set
     * @return The device description
     */
    public DeviceDescription setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Returns the description of the device description.
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the device description.
     *
     * @param description The description to set
     * @return The device description
     */
    public DeviceDescription setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Returns the keywords of the device description.
     *
     * @return The set of keywords
     */
    public Set<String> getKeywords() {
        return keywords;
    }

    /**
     * Sets the keywords of the device description.
     *
     * @param keywords The set of keywords to set
     * @return The device description
     */
    public DeviceDescription setKeywords(Set<String> keywords) {
        this.keywords = keywords;
        return this;
    }

    /**
     * Returns the location details of the device description.
     *
     * @return The location details
     */
    public DeviceDescriptionLocation getLocation() {
        return location;
    }

    /**
     * Sets the location details of the device description.
     *
     * @param location The location details to set
     * @return The device description
     */
    public DeviceDescription setLocation(DeviceDescriptionLocation location) {
        this.location = location;
        return this;
    }

    /**
     * Returns the identifiers collection of the device description.
     *
     * @return The identifiers collection
     */
    public DeviceDescriptionIdentifiers getIdentifiers() {
        return identifiers;
    }

    /**
     * Sets the identifiers collection of the device description.
     *
     * @param identifiers The identifiers collection to set
     * @return The device description
     */
    public DeviceDescription setIdentifiers(DeviceDescriptionIdentifiers identifiers) {
        this.identifiers = identifiers;
        return this;
    }

    /**
     * Returns the capabilities of the device description.
     *
     * @return The list of capabilities
     */
    public List<DeviceDescriptionCapability> getCapabilities() {
        return capabilities;
    }

    /**
     * Sets the capabilities of the device description.
     *
     * @param capabilities The list of capabilities to set
     * @return The device description
     */
    public DeviceDescription setCapabilities(List<DeviceDescriptionCapability> capabilities) {
        this.capabilities = capabilities;
        return this;
    }

    /**
     * Returns the attachments of the device description.
     *
     * @return The list of attachments
     */
    public List<DeviceDescriptionAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Sets the attachments of the device description.
     *
     * @param attachments The list of attachments to set
     * @return The device description
     */
    public DeviceDescription setAttachments(List<DeviceDescriptionAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Returns the SSH details of the device description.
     *
     * @return The SSH details
     */
    public DeviceDescriptionSSHDetails getSshDetails() {
        return sshDetails;
    }

    /**
     * Sets the SSH details of the device description.
     *
     * @param sshDetails The SSH details to set
     * @return The device description
     */
    public DeviceDescription setSshDetails(DeviceDescriptionSSHDetails sshDetails) {
        this.sshDetails = sshDetails;
        return this;
    }

    /**
     * Returns the timestamp indicating when the device description was updated for the last time.
     *
     * @return The timestamp
     */
    public Instant getLastUpdateTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Sets the timestamp indicating when the device description was updated for the last time.
     *
     * @param lastUpdateTimestamp The timestamp to set
     * @return The device description
     */
    public DeviceDescription setLastUpdateTimestamp(Instant lastUpdateTimestamp) {
        this.lastUpdateTimestamp = lastUpdateTimestamp;
        return this;
    }

    /**
     * Returns whether a given object equals the device description by comparing the MAC addresses of the
     * {@link DeviceDescriptionIdentifiers}, if available.
     *
     * @param o The object to compare the device description against
     * @return True, if the given device description equals this device description; false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeviceDescription)) return false;
        DeviceDescription that = (DeviceDescription) o;
        if ((identifiers == null) || (that.identifiers == null) || (identifiers.getMacAddress() == null) || (that.identifiers.getMacAddress() == null))
            return false;
        return identifiers.getMacAddress().equalsIgnoreCase(that.identifiers.getMacAddress());
    }

    /**
     * Calculates and returns a hash code for the device description, preferably based solely on the MAC address
     * of the {@link DeviceDescriptionIdentifiers}.
     *
     * @return The resulting hash code
     */
    @Override
    public int hashCode() {
        //Check if the identifier collection is available
        if (this.identifiers != null) {
            //Use only the MAC address for calculating the hash code
            return this.identifiers.hashCode();
        }

        //MAC address is not available, thus use all fields
        return Objects.hash(name, description, keywords, location, identifiers, capabilities, attachments, sshDetails, lastUpdateTimestamp);
    }
}
