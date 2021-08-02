package de.ipvs.as.mbp.domain.discovery.collections.revision.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.Objects;
import java.util.Set;

/**
 * Objects of this class represent replace operations on a {@link CandidateDevicesCollection} that was received
 * from a discovery repository on behalf of certain {@link DeviceTemplate}. For this, each {@link ReplaceOperation}
 * holds a set of {@link DeviceDescription}s that is supposed to replace the {@link DeviceDescription}s of all
 * candidate devices that have been previously added to the {@link CandidateDevicesCollection}s as a whole.
 */
@JsonIgnoreProperties
public class ReplaceOperation implements RevisionOperation {
    //Type name of this operation
    private static final String TYPE_NAME = "replace";

    //Device descriptions of the candidate devices to use for replacing
    private Set<DeviceDescription> deviceDescriptions;

    /**
     * Creates a new {@link ReplaceOperation} from a given set of {@link DeviceDescription}s that is supposed
     * to replace the existing candidate devices as a whole.
     *
     * @param deviceDescriptions The set of {@link DeviceDescription}s to use for replacing
     */
    @JsonCreator
    public ReplaceOperation(@JsonProperty("deviceDescriptions") Set<DeviceDescription> deviceDescriptions) {
        setDeviceDescriptions(deviceDescriptions);
    }

    /**
     * Returns the set of {@link DeviceDescription}s that is supposed to replace the existing candidate devices
     * as a whole.
     *
     * @return The set of {@link DeviceDescription}s
     */
    public Set<DeviceDescription> getDeviceDescriptions() {
        return deviceDescriptions;
    }

    /**
     * Sets the set of {@link DeviceDescription}s that is supposed to replace the existing candidate devices
     * as a whole.
     *
     * @param deviceDescriptions The set of {@link DeviceDescription}s to set
     * @return The operation
     */
    public ReplaceOperation setDeviceDescriptions(Set<DeviceDescription> deviceDescriptions) {
        //Null checks
        if ((deviceDescriptions == null) || deviceDescriptions.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The device descriptions must not be null.");
        }

        this.deviceDescriptions = deviceDescriptions;
        return this;
    }

    /**
     * Returns the type name of the operation.
     *
     * @return The type name
     */
    @JsonProperty("type")
    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }

    /**
     * Applies the operation to a given {@link CandidateDevicesCollection} that was received from a discovery repository
     * on behalf of a certain {@link DeviceTemplate}.
     *
     * @param collection The {@link CandidateDevicesCollection} to which the operation is supposed to be applied
     */
    @Override
    public void apply(CandidateDevicesCollection collection) {
        //Null check
        if (collection == null) return;

        //Remove all existing candidate devices from the collection
        collection.clear();

        //Add the descriptions of the new candidate devices to the collection
        collection.addAll(this.deviceDescriptions);
    }

    /**
     * Returns a human-readable string representation of the {@link RevisionOperation}.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableDescription() {
        return String.format("Replace with %d candidate devices", this.deviceDescriptions.size());
    }
}
