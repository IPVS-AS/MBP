package de.ipvs.as.mbp.domain.discovery.deployment;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionIdentifiers;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionSSHDetails;

/**
 * Collection of details about a device on which a {@link DynamicDeployment} is, was or may be deployed.
 */
public class DynamicDeploymentDeviceDetails {
    //MAC address of the device
    private String macAddress;

    //IP address of the device
    private String ipAddress;

    //Username for SSH
    private String username;

    //Password for SSH authentication
    private String password;

    //Private key for SSH authentication
    private String privateKey;

    /**
     * Creates a new, empty device details object.
     */
    public DynamicDeploymentDeviceDetails() {

    }

    /**
     * Creates a new device details object from a given {@link DeviceDescription} by copying the relevant fields.
     *
     * @param deviceDescription The device description to use
     */
    public DynamicDeploymentDeviceDetails(DeviceDescription deviceDescription) {
        //Null checks
        if (deviceDescription == null) {
            throw new IllegalArgumentException("The device description must not be null.");
        } else if (deviceDescription.getIdentifiers() == null) {
            throw new IllegalArgumentException("The device description must contain identifiers.");
        } else if (deviceDescription.getSshDetails() == null) {
            throw new IllegalArgumentException("The device description must contain SSH details.");
        }

        //Unpack the device description
        DeviceDescriptionIdentifiers identifiers = deviceDescription.getIdentifiers();
        DeviceDescriptionSSHDetails sshDetails = deviceDescription.getSshDetails();

        //Copy fields
        this.macAddress = identifiers.getMacAddress();
        this.ipAddress = sshDetails.getIpAddress();
        this.username = sshDetails.getUsername();
        this.password = sshDetails.getPassword();
        this.privateKey = sshDetails.getPrivateKey();
    }

    /**
     * Returns the MAC address of the device details.
     *
     * @return The MAC address
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Sets the MAC address of the device details
     *
     * @param macAddress The MAC address to set
     * @return The device details
     */
    public DynamicDeploymentDeviceDetails setMacAddress(String macAddress) {
        this.macAddress = macAddress;
        return this;
    }

    /**
     * Returns the IP address of the device details.
     *
     * @return The IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address of the device details.
     *
     * @param ipAddress The IP address to set
     * @return The device details
     */
    public DynamicDeploymentDeviceDetails setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * Returns the username of the device details.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the device details.
     *
     * @param username The username to set
     * @return The device details
     */
    public DynamicDeploymentDeviceDetails setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Returns the password of the device details.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the device details.
     *
     * @param password The password to set
     * @return The device details
     */
    public DynamicDeploymentDeviceDetails setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Returns the private key of the device details.
     *
     * @return The private key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private key of the device details
     *
     * @param privateKey The private key to set
     * @return The device details
     */
    public DynamicDeploymentDeviceDetails setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }
}
