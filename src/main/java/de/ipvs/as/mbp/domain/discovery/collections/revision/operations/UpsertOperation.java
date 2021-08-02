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
 * Objects of this class represent upsert operations on a {@link CandidateDevicesCollection} that was received
 * from a discovery repository on behalf of certain {@link DeviceTemplate}. For this, each {@link UpsertOperation}
 * holds a set of {@link DeviceDescription}s that are supposed to be individually used in order to either update
 * the {@link DeviceDescription} of already known candidate devices, or to add {@link DeviceDescription}s for new
 * candidate devices to the collection.
 */
@JsonIgnoreProperties
public class UpsertOperation implements RevisionOperation {
    //Type name of this operation
    private static final String TYPE_NAME = "upsert";

    //Device descriptions of the candidate devices to upsert
    private Set<DeviceDescription> deviceDescriptions;

    /**
     * Creates a new {@link UpsertOperation} from a given set of {@link DeviceDescription}s to upsert.
     *
     * @param deviceDescriptions The set of {@link DeviceDescription}s to upsert
     */
    @JsonCreator
    public UpsertOperation(@JsonProperty("deviceDescriptions") Set<DeviceDescription> deviceDescriptions) {
        setDeviceDescriptions(deviceDescriptions);
    }

    /**
     * Returns the set of {@link DeviceDescription}s that are supposed to be upserted.
     *
     * @return The set of {@link DeviceDescription}s
     */
    public Set<DeviceDescription> getDeviceDescriptions() {
        return deviceDescriptions;
    }

    /**
     * Sets the set of {@link DeviceDescription}s that are supposed to be upserted.
     *
     * @param deviceDescriptions The set of {@link DeviceDescription}s to set
     * @return The operation
     */
    public UpsertOperation setDeviceDescriptions(Set<DeviceDescription> deviceDescriptions) {
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

        //Iterate over all device descriptions of this operation and upsert them
        this.deviceDescriptions.forEach(d -> {
            //Remove device description from the collection if already existing
            collection.remove(d);
            //Add the new device description to the collection
            collection.add(d);
        });
    }

    /**
     * Returns a human-readable string representation of the {@link RevisionOperation}.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableDescription() {
        return String.format("Upsert %d candidate devices", this.deviceDescriptions.size());
    }
}
