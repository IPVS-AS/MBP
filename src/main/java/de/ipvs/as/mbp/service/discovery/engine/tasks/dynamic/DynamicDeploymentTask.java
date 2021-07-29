package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;

/**
 * Interface for discovery tasks related to {@link DynamicDeployment}s and the deployment of their operators to
 * actual IoT devices. The tasks can be scheduled by the {@link DiscoveryEngine} in a {@link TaskWrapper}
 * for asynchronous execution.
 */
public interface DynamicDeploymentTask extends DiscoveryTask {
    /**
     * Returns the ID of the {@link DynamicDeployment} on which this task operates.
     *
     * @return The dynamic deployment ID
     */
    String getDynamicDeploymentId();

    /**
     * Returns the ID of the device template that is used by the {@link DynamicDeployment} on which this task operates.
     *
     * @return The device template ID
     */
    String getDeviceTemplateId();

    /**
     * Returns whether this task requires access to the candidate devices of the {@link DeviceTemplate} that is
     * referenced in the {@link DynamicDeployment}.
     *
     * @return True, if this task depends on the candidate devices; false otherwise
     */
    boolean dependsOnCandidateDevices();

    /**
     * Returns whether this task may replace another, previously created task in the task queue.
     *
     * @return True, if the task may replace another task; false otherwise
     */
    boolean mayReplace();
}
