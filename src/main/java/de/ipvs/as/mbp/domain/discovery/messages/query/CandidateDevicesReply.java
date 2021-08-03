package de.ipvs.as.mbp.domain.discovery.messages.query;

import de.ipvs.as.mbp.domain.discovery.collections.revision.CandidateDevicesRevision;
import de.ipvs.as.mbp.domain.discovery.collections.revision.operations.ReplaceOperation;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageBody;
import de.ipvs.as.mbp.service.messaging.message.DomainMessageTemplate;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Reply message that is expected to be received in response to {@link CandidateDevicesRequest} messages or as part
 * of an asynchronous notification as resulting from a subscription that has been previously registered at a discovery
 * repository. The reply message contains a set of {@link CandidateDevicesRevision}s, each describing the
 * operations that need to be executed on the candidate devices of one discovery repositories that are stored for one
 * or multiple @link DeviceTemplate}s of the same user in order to keep them up-to-date.
 */
@DomainMessageTemplate(value = "query_reply")
public class CandidateDevicesReply extends DomainMessageBody {
    //Set of revisions for the candidate devices of the pertaining device templates
    private Set<CandidateDevicesRevision> revisions;

    /**
     * Creates a new candidate devices reply.
     */
    public CandidateDevicesReply() {
        //Initialize data structures
        this.revisions = new HashSet<>();
    }

    /**
     * Returns whether the reply contains any {@link CandidateDevicesRevision}s.
     *
     * @return True, if the reply contains {@link CandidateDevicesRevision}s; false otherwise
     */
    public boolean isEmpty() {
        return this.revisions.isEmpty();
    }

    /**
     * Returns the set of {@link CandidateDevicesRevision}s that is part of this reply.
     *
     * @return The {@link CandidateDevicesRevision}s
     */
    public Set<CandidateDevicesRevision> getRevisions() {
        return revisions;
    }

    /**
     * Sets the set of {@link CandidateDevicesRevision}s that is part of this reply.
     *
     * @param revisions The {@link CandidateDevicesRevision}s to set
     */
    public void setRevisions(Set<CandidateDevicesRevision> revisions) {
        //Null checks
        if ((revisions == null) || (revisions.stream().anyMatch(Objects::isNull))) {
            throw new IllegalArgumentException("The candidate devices revisions must not be null.");
        }

        this.revisions = revisions;
    }

    /**
     * Inspects one of the provided {@link CandidateDevicesRevision}s, looks for its first {@link ReplaceOperation} and
     * returns the associated {@link DeviceDescription}s. This is especially useful when the
     * {@link CandidateDevicesReply} was sent as synchronous response to a request and is expected
     * to contain the {@link DeviceDescription}s of all matching candidate devices of the discovery repository.
     *
     * @return The resulting {@link DeviceDescription}s
     */
    public Set<DeviceDescription> getFirstDeviceDescriptions() {
        //Sanity check
        if (this.revisions.isEmpty()) return null;

        //Get first revision
        CandidateDevicesRevision revision = this.revisions.iterator().next();

        //Find first replace operation
        ReplaceOperation replaceOperation = revision.getOperations().stream()
                .filter(o -> o instanceof ReplaceOperation)
                .map(o -> (ReplaceOperation) o)
                .findFirst().orElse(null);

        //Check if replace operation could be found
        if (replaceOperation == null) return null;

        //Return the device descriptions of the first replace operation
        return replaceOperation.getDeviceDescriptions();
    }
}
