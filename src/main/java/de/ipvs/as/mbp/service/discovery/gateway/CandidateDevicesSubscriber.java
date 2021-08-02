package de.ipvs.as.mbp.service.discovery.gateway;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.collections.revision.CandidateDevicesRevision;
import de.ipvs.as.mbp.domain.discovery.collections.revision.operations.RevisionOperation;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

/**
 * Objects of this class are subscribers that are notified via a callback method when the collection of
 * suitable candidate devices, which could be determined on behalf of a given {@link DeviceTemplate}, changes over time
 * at a certain repository.
 */
public interface CandidateDevicesSubscriber {
    /**
     * Called in case a notification was received from a discovery repository as result of a subscription,
     * indicating that the corresponding {@link CandidateDevicesCollection} of a certain {@link DeviceTemplate}
     * changed over time. Thereby, a {@link CandidateDevicesRevision} is provided, containing the
     * {@link RevisionOperation}s that can be executed on the {@link CandidateDevicesCollection} in order to update
     * it again.
     *
     * @param deviceTemplateId The ID of the {@link DeviceTemplate} whose candidate devices are affected
     * @param repositoryName   The name of the repository that issued the notification
     * @param revision         The {@link CandidateDevicesRevision} that allows to update the candidate devices again
     */
    void onCandidateDevicesChanged(String deviceTemplateId, String repositoryName, CandidateDevicesRevision revision);
}
