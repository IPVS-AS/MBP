package de.ipvs.as.mbp.service.discovery.gateway;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesCollection;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;

/**
 * Objects of this class represent subscribers that are notified via a callback method when the collection of
 * suitable candidate devices, which could be determined on behalf of a given {@link DeviceTemplate}, changed over time
 * at a certain repository.
 */
public interface CandidateDevicesSubscriber {
    /**
     * Called in case a notification was received from a repository as result of a subscription,
     * indicating that the collection of suitable candidate devices, which could be determined on behalf of a
     * certain {@link DeviceTemplate}, changed over time.
     *
     * @param deviceTemplate          The device template for which the candidate devices are retrieved
     * @param repositoryName          The name of the repository that issued the notification
     * @param updatedCandidateDevices The updated collection of candidate devices as {@link CandidateDevicesCollection}
     */
    void onDeviceTemplateResultChanged(DeviceTemplate deviceTemplate, String repositoryName,
                                       CandidateDevicesCollection updatedCandidateDevices);
}
