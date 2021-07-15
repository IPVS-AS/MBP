package de.ipvs.as.mbp.service.discovery.deployment;

/**
 * Enumeration of possible results of deployment tasks that are scheduled at the {@link DiscoveryDeploymentExecutor}.
 */
public enum DeploymentResult {
    DEPLOYED, EMPTY_RANKING, ALL_FAILED;
}
