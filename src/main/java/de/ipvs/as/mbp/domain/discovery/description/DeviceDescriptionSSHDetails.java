package de.ipvs.as.mbp.domain.discovery.description;

/**
 * Objects of this class are typically part of {@link DeviceDescription}s and collect data that is required to
 * establish a SSH connection to the described device.
 */
public class DeviceDescriptionSSHDetails {
    //IP address of the device within the network that is shared with the MBP
    private String ipAddress;

    //Username for SSH login
    private String username;

    //Password for SSH login
    private String password;

    //Private key for SSH login
    private String privateKey;

    /**
     * Creates a new, empty SSH details object.
     */
    public DeviceDescriptionSSHDetails() {

    }

    /**
     * Returns the IP address of the SSH details object.
     *
     * @return The IP address
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address of the SSH details object.
     *
     * @param ipAddress The IP address to set
     * @return The SSH details object
     */
    public DeviceDescriptionSSHDetails setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    /**
     * Returns the username of the SSH details object.
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the SSH details object.
     *
     * @param username The username to set
     * @return The SSH details object
     */
    public DeviceDescriptionSSHDetails setUsername(String username) {
        this.username = username;
        return this;
    }

    /**
     * Returns the password of the SSH details object.
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password of the SSH details object.
     *
     * @param password The password to set
     * @return The SSH details object
     */
    public DeviceDescriptionSSHDetails setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Returns the private SSH key of the details object.
     *
     * @return The private SSH key
     */
    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * Sets the private SSH key of the details object.
     *
     * @param privateKey The private SSH key to set
     * @return The SSH details object
     */
    public DeviceDescriptionSSHDetails setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
        return this;
    }
}
