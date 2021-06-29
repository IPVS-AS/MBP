package de.ipvs.as.mbp.domain.discovery.description;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Objects of this class collect various identifiers and characteristics of a device within {@link DeviceDescription}s.
 */
public class DeviceDescriptionIdentifiers {
    //The type of the device (e.g. Raspberry Pi)
    private String type;

    //The model name of the device
    @JsonProperty("model")
    private String modelName;

    //The name of the device's manufacturer
    private String manufacturer;

    //The name and version of the device's operating system
    @JsonProperty("osName")
    private String operatingSystemName;

    //The MAC address of the device
    private String macAddress;

    /**
     * Creates a new, empty identifier collection object.
     */
    public DeviceDescriptionIdentifiers() {

    }

    /**
     * Returns the type of the device.
     *
     * @return The device type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the device.
     *
     * @param type The device type to set
     * @return The identifier collection object
     */
    public DeviceDescriptionIdentifiers setType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Returns the model name of the device.
     *
     * @return The model name
     */
    public String getModelName() {
        return modelName;
    }

    /**
     * Sets the model name of the device.
     *
     * @param modelName The model name to set
     * @return The identifier collection object
     */
    public DeviceDescriptionIdentifiers setModelName(String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Returns the name of the device's manufacturer.
     *
     * @return The name of the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * Sets the name of the device's manufacturer.
     *
     * @param manufacturer The name of the manufacturer to set
     * @return The identifier collection object
     */
    public DeviceDescriptionIdentifiers setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    /**
     * Returns the name of the operating system that runs on the device.
     *
     * @return The operating system name
     */
    public String getOperatingSystemName() {
        return operatingSystemName;
    }

    /**
     * Sets the name of the operating system that runs on the device.
     *
     * @param operatingSystemName The name of the operating system to set
     * @return The identifier collection object
     */
    public DeviceDescriptionIdentifiers setOperatingSystemName(String operatingSystemName) {
        this.operatingSystemName = operatingSystemName;
        return this;
    }

    /**
     * Returns the MAC address of the device.
     *
     * @return The MAC address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets the MAC address of the device.
     *
     * @param macAddress The MAC address to set
     * @return The identifier collection object
     */
    public DeviceDescriptionIdentifiers setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }
}
