package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryTask;
import de.ipvs.as.mbp.service.discovery.engine.tasks.TaskWrapper;

/**
 * Interface for discovery tasks related to {@link DynamicPeripheral}s and the deployment of their operators to
 * actual IoT devices. The tasks can be scheduled by the {@link DiscoveryEngine} in a {@link TaskWrapper}
 * for asynchronous execution.
 */
public interface DynamicPeripheralTask extends DiscoveryTask {
    /**
     * Returns the ID of the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The dynamic peripheral ID
     */
    String getDynamicPeripheralId();

    /**
     * Returns the ID of the device template that is used by the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The device template ID
     */
    String getDeviceTemplateId();
}
