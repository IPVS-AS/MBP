package de.ipvs.as.mbp.service.deployment;

/**
 * Enumerates all possible availability states of devices.
 * <p>
 * Semantics:
 * - Unknown: State of the device is not known
 * - Offline: The device is offline, no connection possible
 * - Online: Device is reachable, but no SSH connection can be established
 * - SSH-Available: Device is available and a SSH connection can be established successfully
 */
public enum DeviceState {
    UNKNOWN, OFFLINE, ONLINE, SSH_AVAILABLE
}
