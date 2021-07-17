package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;

/**
 * Interface for discovery tasks related to {@link DeviceTemplate}s and the {@link CandidateDevicesResult}s
 * that are stored from them. The tasks can be scheduled by the {@link DiscoveryEngine} in a
 * {@link TaskWrapper} for asynchronous execution.
 */
public interface DeviceTemplateTask extends DiscoveryTask {
    /**
     * Returns the ID of the {@link DeviceTemplate} on which this task operates.
     *
     * @return The ID of the device template
     */
    String getDeviceTemplateId();
}
