package de.ipvs.as.mbp.domain.discovery.collections.revision.operations;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescriptionIdentifiers;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.Objects;
import java.util.Set;

/**
 * Objects of this class represent delete operations on a {@link CandidateDevicesCollection} that was received
 * from a discovery repository on behalf of certain {@link DeviceTemplate}. For this, each {@link DeleteOperation}
 * holds a set of MAC addresses, referencing the candidate devices whose {@link DeviceDescription}s are supposed
 * to be removed from the collection.
 */
@JsonIgnoreProperties
public class DeleteOperation extends RevisionOperation {
    //Type name of this operation
    private static final String TYPE_NAME = "delete";

    //MAC addresses of the candidate devices to delete
    private Set<String> macAddresses;

    /**
     * Creates a new, empty {@link DeleteOperation}.
     */
    public DeleteOperation() {

    }

    /**
     * Creates a new {@link DeleteOperation} from a given set of MAC addresses, referencing the candidate devices
     * whose {@link DeviceDescription}s are supposed to be removed.
     *
     * @param macAddresses The set of MAC addresses of the candidate devices to delete
     */
    @JsonCreator
    public DeleteOperation(@JsonProperty("macAddresses") Set<String> macAddresses) {
        setMacAddresses(macAddresses);
    }

    /**
     * Returns the set of MAC addresses referencing the candidate devices to delete.
     *
     * @return The set of MAC addresses
     */
    public Set<String> getMacAddresses() {
        return macAddresses;
    }

    /**
     * Sets the set of MAC addresses referencing the candidate devices to delete.
     *
     * @param macAddresses The set of MAC addresses to set
     * @return The operation
     */
    public DeleteOperation setMacAddresses(Set<String> macAddresses) {
        //Null checks
        if ((macAddresses == null) || macAddresses.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("The MAC addresses must not be null or empty.");
        }

        this.macAddresses = macAddresses;
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

        //Stream through the given MAC addresses
        this.macAddresses.stream()
                .map(m -> new DeviceDescription() //Map the MAC address to a new dummy device description
                        .setIdentifiers(new DeviceDescriptionIdentifiers().setMacAddress(m)))
                .forEach(collection::remove); //Remove the device description matching the dummy
    }

    /**
     * Returns a human-readable string representation of the {@link RevisionOperation}.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableDescription() {
        return String.format("Delete %d candidate device" + (this.macAddresses.size() == 1 ? "" : "s"),
                this.macAddresses.size());
    }
}
