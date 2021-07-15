package de.ipvs.as.mbp.service.discovery.deployment;

import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;

/**
 * This interface represents listeners that are notified via callback method in case a deployment task
 * of the {@link DiscoveryDeploymentExecutor} finished.
 */
public interface DeploymentCompletionListener {
    /**
     * Called in case a certain deployment task, which was scheduled at the {@link DiscoveryDeploymentExecutor},
     * completed.
     *
     * @param dynamicPeripheral The dynamic peripheral that was supposed to be deployed
     * @param result            The result of the deployment
     */
    void onDeploymentCompleted(DynamicPeripheral dynamicPeripheral, DeploymentResult result);
}
