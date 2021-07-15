package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralStatus;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import de.ipvs.as.mbp.service.discovery.engine.tasks.DiscoveryEngineTask;

import java.util.concurrent.FutureTask;

/**
 * Interface for discovery tasks related to {@link DynamicPeripheral}s and the deployment of their operators to
 * actual IoT devices. The tasks can be scheduled by the {@link DiscoveryEngine} as cancellable {@link FutureTask}s
 * for asynchronous execution.
 */
public interface DynamicPeripheralTask extends DiscoveryEngineTask<DynamicPeripheralTask> {
    /**
     * Returns the final {@link DynamicPeripheralStatus} in which the pertaining {@link DynamicPeripheral}
     * is after the completion of this task.
     *
     * @return The result state
     */
    DynamicPeripheralStatus getResultState();
}
