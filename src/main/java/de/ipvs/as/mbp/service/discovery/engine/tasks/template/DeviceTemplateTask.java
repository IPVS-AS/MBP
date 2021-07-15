package de.ipvs.as.mbp.service.discovery.engine.tasks.template;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResultContainer;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryEngineTask;

import java.util.concurrent.FutureTask;

/**
 * Interface for discovery tasks related to {@link DeviceTemplate}s and the {@link CandidateDevicesResultContainer}s
 * that are stored from them. The tasks can be scheduled by the {@link DiscoveryEngine} as cancellable
 * {@link FutureTask}s for asynchronous execution.
 */
public interface DeviceTemplateTask extends DiscoveryEngineTask<DeviceTemplateTask> {
    /**
     * Returns the ID of the device template on which this tasks operates.
     *
     * @return The ID of the device template
     */
    String getDeviceTemplateId();
}
