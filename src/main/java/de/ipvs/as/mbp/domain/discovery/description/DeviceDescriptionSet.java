package de.ipvs.as.mbp.domain.discovery.description;

import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Objects of this class represent sets of device descriptions that were received from a certain repository, identified
 * by its name, for a certain {@link DeviceTemplate}.
 */
public class DeviceDescriptionSet {
    private String repositoryName;
    private String deviceTemplateId;
    private Set<DeviceDescription> deviceDescriptions;

    /**
     * Creates a new device description set from a given repository name and a given device template ID.
     *
     * @param repositoryName   The repository name to use
     * @param deviceTemplateId The device template ID to use
     */
    public DeviceDescriptionSet(String repositoryName, String deviceTemplateId) {
        //Initialize set of device descriptions
        this.deviceDescriptions = new HashSet<>();

        //Set fields
        setRepositoryName(repositoryName);
        setDeviceTemplateId(deviceTemplateId);
    }

    /**
     * Returns the name of the repository from which the set of device descriptions were received.
     *
     * @return The repository name
     */
    public String getRepositoryName() {
        return repositoryName;
    }

    /**
     * Sets the name of the repository from which the set of device descriptions were received.
     *
     * @param repositoryName The name of the repository to set
     * @return The device description set
     */
    public DeviceDescriptionSet setRepositoryName(String repositoryName) {
        //Sanity check
        if ((repositoryName == null) || (repositoryName.isEmpty())) {
            throw new IllegalArgumentException("Repository name must nut be null or empty.");
        }

        //Set repository name
        this.repositoryName = repositoryName;
        return this;
    }

    /**
     * Returns the ID of the device template for which the device descriptions were received.
     *
     * @return The ID of the device template
     */
    public String getDeviceTemplateId() {
        return deviceTemplateId;
    }

    /**
     * Sets the ID of the device template for which the device descriptions were received.
     *
     * @param deviceTemplateId The ID of the device template to set
     * @return The device description set
     */
    public DeviceDescriptionSet setDeviceTemplateId(String deviceTemplateId) {
        this.deviceTemplateId = deviceTemplateId;
        return this;
    }

    /**
     * Returns the set of device descriptions that were received from the repository.
     *
     * @return The set of device descriptions
     */
    public Set<DeviceDescription> getDeviceDescriptions() {
        return deviceDescriptions;
    }

    /**
     * Sets the set of device descriptions that were received from the repository.
     *
     * @param deviceDescriptions The set of device descriptions to set
     * @return The device description set
     */
    public DeviceDescriptionSet setDeviceDescriptions(Set<DeviceDescription> deviceDescriptions) {
        this.deviceDescriptions = deviceDescriptions;
        return this;
    }

    /**
     * Adds a collection of device descriptions to the set.
     *
     * @param deviceDescriptions The collection of device descriptions to add
     * @return The device description set
     */
    public DeviceDescriptionSet addDeviceDescriptions(Collection<DeviceDescription> deviceDescriptions) {
        //Add all device descriptions
        this.deviceDescriptions.addAll(deviceDescriptions);
        return this;
    }
}
