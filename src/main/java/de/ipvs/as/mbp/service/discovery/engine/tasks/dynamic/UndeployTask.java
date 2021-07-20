package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentState;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;

/**
 * The purpose of this task is to undeploy the {@link Operator} of a given {@link DynamicDeployment}
 * from the device that is referenced as {@link DynamicDeploymentDeviceDetails} in the {@link DynamicDeployment},
 * in case the {@link DynamicDeployment} is deployed on it.
 */
public class UndeployTask implements DynamicDeploymentTask {

    //The original version of the dynamic deployment that is supposed to be deployed
    private DynamicDeployment originalDynamicDeployment;

    /*
    Injected fields
     */
    private final DiscoveryDeploymentService discoveryDeploymentService;
    private final DynamicDeploymentRepository dynamicDeploymentRepository;

    /**
     * Creates a new {@link UndeployTask} from a given {@link DynamicDeployment}.
     *
     * @param dynamicDeployment The dynamic deployment to use
     */
    public UndeployTask(DynamicDeployment dynamicDeployment) {
        //Set fields
        setDynamicDeployment(dynamicDeployment);

        //Inject components
        this.discoveryDeploymentService = DynamicBeanProvider.get(DiscoveryDeploymentService.class);
        this.dynamicDeploymentRepository = DynamicBeanProvider.get(DynamicDeploymentRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read dynamic deployment from the repository
        DynamicDeployment dynamicDeployment = dynamicDeploymentRepository.findById(this.originalDynamicDeployment.getId()).orElse(null);

        //Sanity checks
        if (dynamicDeployment == null) {
            //Task ends because data is not available
            return;
        }

        //Check intention for dynamic deployment
        if (dynamicDeployment.isActivatingIntended()) {
            //De-activate is not intended, so no need to undeploy
            return;
        }

        //Update the state of the dynamic deployment
        this.updateDynamicDeployment(dynamicDeployment.setState(DynamicDeploymentState.IN_PROGRESS));

        //Check whether the dynamic deployment is currently deployed
        if ((dynamicDeployment.getLastDeviceDetails() == null) ||
                (!this.discoveryDeploymentService.isDeployed(dynamicDeployment))) {
            //Dynamic deployment is not deployed, so just update the state
            updateDynamicDeployment(dynamicDeployment.setLastDeviceDetails(null)
                    .setState(DynamicDeploymentState.DISABLED));
            return;
        }

        //Dynamic deployment is deployed, so undeploy it
        this.discoveryDeploymentService.undeploy(dynamicDeployment);

        //Update state of dynamic deployment accordingly
        updateDynamicDeployment(dynamicDeployment.setLastDeviceDetails(null)
                .setState(DynamicDeploymentState.DISABLED));
    }

    /**
     * Writes a given {@link DynamicDeployment} to the repository, thus updating its fields in the database.
     *
     * @param dynamicDeployment The dynamic deployment to update
     */
    private void updateDynamicDeployment(DynamicDeployment dynamicDeployment) {
        //Write state to repository
        this.dynamicDeploymentRepository.save(dynamicDeployment);
    }

    /**
     * Returns the {@link DynamicDeployment} object that has been originally passed to this task and is
     * supposed to be (re-)deployed.
     *
     * @return The dynamic deployment
     */
    public DynamicDeployment getDynamicDeployment() {
        return this.originalDynamicDeployment;
    }

    /**
     * Sets the {@link DynamicDeployment} that is supposed to be (re-)deployed within this task.
     *
     * @param dynamicDeployment The dynamic deployment to set
     */
    private void setDynamicDeployment(DynamicDeployment dynamicDeployment) {
        //Null check
        if (dynamicDeployment == null) {
            throw new IllegalArgumentException("The dynamic deployment must not be null.");
        }

        this.originalDynamicDeployment = dynamicDeployment;
    }

    /**
     * Returns the ID of the {@link DynamicDeployment} on which this task operates.
     *
     * @return The dynamic deployment ID
     */
    @Override
    public String getDynamicDeploymentId() {
        return this.originalDynamicDeployment.getId();
    }


    /**
     * Returns the ID of the device template that is used by the {@link DynamicDeployment} on which this task operates.
     *
     * @return The device template ID
     */
    @Override
    public String getDeviceTemplateId() {
        return this.originalDynamicDeployment.getDeviceTemplate().getId();
    }

    /**
     * Returns a simple, short and human-readable description of the task.
     *
     * @return The human-readable description
     */
    @Override
    public String toHumanReadableString() {
        return "[Undeploy]";
    }
}
