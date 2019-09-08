package org.citopt.connde.service.deploy;

/**
 * Enumerates all possible availability states of components.
 *
 * Semantics:
 * - Unknown: State of the component is not known
 * - Not Ready: The component cannot be deployed (device unavailable, wrong configuration, ...)
 * - Ready: The component is ready for deployment, device is available and correctly configured
 * - Deployed: Component is already deployed, but not running (e.g. paused)
 * - Running: Component is deployed and running
 */
public enum ComponentState {
    UNKNOWN, NOT_READY, READY, DEPLOYED, RUNNING;
}
