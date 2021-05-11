package de.ipvs.as.mbp.service.rules.execution.component_deployment;

import de.ipvs.as.mbp.service.deployment.ComponentState;

/**
 * Enumeration of the available deployment actions within the component deployment executor. Each deployment action
 * references a target component state in which a component is supposed to be after the action execution.
 */
enum DeploymentAction {
    DEPLOY(ComponentState.DEPLOYED), START(ComponentState.RUNNING), STOP(ComponentState.DEPLOYED),
    UNDEPLOY(ComponentState.READY);

    private ComponentState targetState;

    /**
     * Creates a new deployment action enumeration object by passing a desired target state in which a component
     * is supposed to be after the action execution.
     *
     * @param targetState The target state
     */
    DeploymentAction(ComponentState targetState) {
        this.targetState = targetState;
    }

    /**
     * Returns the target state of the deployment action.
     *
     * @return The target state
     */
    public ComponentState getTargetState() {
        return this.targetState;
    }
}