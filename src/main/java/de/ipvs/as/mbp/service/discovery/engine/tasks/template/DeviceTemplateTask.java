package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResultContainer;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;

/**
 * Interface for discovery tasks related to {@link DeviceTemplate}s and the {@link CandidateDevicesResultContainer}s
 * that are stored from them. The tasks can be scheduled by the {@link DiscoveryEngine} in a
 * {@link TaskWrapper} for asynchronous execution.
 */
public interface DeviceTemplateTask extends DiscoveryTask {
    /**
     * Returns the {@link DeviceTemplate} on which this tasks operates.
     *
     * @return The device template
     */
    DeviceTemplate getDeviceTemplate();
}
