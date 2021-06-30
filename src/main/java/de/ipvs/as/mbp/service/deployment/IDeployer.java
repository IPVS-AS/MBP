package de.ipvs.as.mbp.service.deployment;

import de.ipvs.as.mbp.domain.component.Component;
import de.ipvs.as.mbp.domain.device.Device;
import de.ipvs.as.mbp.domain.operator.parameters.ParameterInstance;

import java.util.List;

/**
 * Basic interface for deployers that are able to deploy MBP components onto their corresponding devices.
 */
public interface IDeployer {
    /**
     * Retrieves the current deployment state of a given component.
     *
     * @param component The component to retrieve the deployment state for
     * @return The current deployment state of the component
     */
    ComponentState retrieveComponentState(Component component);

    /**
     * Retrieves the current availability state of a given device.
     *
     * @param device The device to retrieve the availability state for
     * @return The current availability state of the device
     */
    DeviceState retrieveDeviceState(Device device);

    /**
     * Starts a component on its corresponding remote device and passes deployment
     * parameters to it.
     *
     * @param component             The component to start
     * @param parameterInstanceList List of parameter instances to pass
     */
    void startComponent(Component component, List<ParameterInstance> parameterInstanceList);

    /**
     * Stops a component on its corresponding remote device.
     *
     * @param component The component to stop
     */
    void stopComponent(Component component);

    /**
     * Deploys a component onto its corresponding remote device.
     *
     * @param component The component to deploy
     */
    void deployComponent(Component component);

    /**
     * Undeploys a component from its corresponding remote device.
     *
     * @param component The component to undeploy
     */
    void undeployComponent(Component component);

    /**
     * Undeploys a component from its corresponding remote device if it is currently running.
     *
     * @param component The component to undeploy
     */
    void undeployIfRunning(Component component);

    /**
     * Checks whether a given component is currently running on its corresponding remote device.
     *
     * @param component The component to check
     * @return True, if the component is running; false otherwise
     */
    boolean isComponentRunning(Component component);

    /**
     * Checks whether a given component is currently deployed on its corresponding remote device.
     *
     * @param component The component to check
     * @return True, if the component is deployed; false otherwise
     */
    boolean isComponentDeployed(Component component);
}
