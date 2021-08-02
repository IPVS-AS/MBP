package de.ipvs.as.mbp.domain.discovery.collections.revision;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.revision.operations.RevisionOperation;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Objects of this class collect {@link RevisionOperation}s that are supposed to be executed on the
 * {@link CandidateDevicesCollection} of one discovery repository that are stored for one or
 * multiple {@link DeviceTemplate}s.
 */
public class CandidateDevicesRevision {
    //The IDs of the pertaining device templates
    private Set<String> referenceIds;

    //The list of operations to execute on the candidate devices
    private List<RevisionOperation> operations;

    /**
     * Creates a new, empty {@link CandidateDevicesRevision}.
     */
    public CandidateDevicesRevision() {
        //Initialize data structures
        this.referenceIds = new HashSet<>();
        this.operations = new LinkedList<>();
    }

    /**
     * Applies the {@link RevisionOperation}s of this revision to a given collection of
     * {@link CandidateDevicesCollection}s by executing them one by one.
     *
     * @param candidateDevicesCollections The {@link CandidateDevicesCollection}s to which the operations
     *                                    are supposed to be applied
     */
    public void applyOperations(Collection<CandidateDevicesCollection> candidateDevicesCollections) {
        //Null check
        if (candidateDevicesCollections == null)
            throw new IllegalArgumentException("The candidate devices collections must not be null.");

        //Apply the operations to all provided candidate devices
        candidateDevicesCollections.stream().filter(Objects::nonNull).forEach(this::applyOperations);
    }

    /**
     * Applies the {@link RevisionOperation}s of this revision to a given {@link CandidateDevicesCollection}s
     * by executing them one by one.
     *
     * @param candidateDevicesCollection The {@link CandidateDevicesCollection} to which the operations are supposed
     *                                   to be applied
     */
    public void applyOperations(CandidateDevicesCollection candidateDevicesCollection) {
        //Null check
        if (candidateDevicesCollection == null)
            throw new IllegalArgumentException("The candidate devices collection must not be null.");

        //Apply all operations in order
        this.operations.forEach(o -> o.apply(candidateDevicesCollection));
    }


    /**
     * Returns a human-readable string representation of the {@link CandidateDevicesRevision}, summarizing
     * the contained {@link RevisionOperation}s.
     *
     * @return The human-readable description
     */
    public String toHumanReadableDescription() {
        //Stream through the operations and join their human-readable descriptions
        return this.operations.stream().map(RevisionOperation::toHumanReadableDescription).map(d -> "- " + d)
                .collect(Collectors.joining("\n"));
    }


    /**
     * Returns the set of reference IDs, containing the IDs of the pertaining {@link DeviceTemplate}s.
     *
     * @return The set of reference IDs
     */
    public Set<String> getReferenceIds() {
        return referenceIds;
    }

    /**
     * Sets the set of reference IDs, containing the IDs of the pertaining {@link DeviceTemplate}s.
     *
     * @param referenceIds The set of reference IDs to set
     * @return The revision
     */
    public CandidateDevicesRevision setReferenceIds(Set<String> referenceIds) {
        this.referenceIds = referenceIds;
        return this;
    }

    /**
     * Returns the list of {@link RevisionOperation}s that are supposed to be executed on the candidate devices.
     *
     * @return The list of {@link RevisionOperation}s
     */
    public List<RevisionOperation> getOperations() {
        return operations;
    }

    /**
     * Sets the list of {@link RevisionOperation}s that are supposed to be executed on the candidate devices.
     *
     * @param operations The list of {@link RevisionOperation}s to set
     * @return The revision
     */
    public CandidateDevicesRevision setOperations(List<RevisionOperation> operations) {
        this.operations = operations;
        return this;
    }
}
