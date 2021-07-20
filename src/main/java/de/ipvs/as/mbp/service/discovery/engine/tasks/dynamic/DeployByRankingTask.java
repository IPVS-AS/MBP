package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeployment;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.deployment.DynamicDeploymentState;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicDeploymentRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;

import java.util.Optional;

/**
 * The purpose of this task is to deploy a given {@link DynamicDeployment} with respect to the ranking which
 * can be computed from the candidate devices in the {@link CandidateDevicesResult} that is associated
 * with the {@link DeviceTemplate} underlying the {@link DynamicDeployment}. Thereby, the operator of the
 * {@link DynamicDeployment} is supposed to be deployed to the target device of the ranking that can be considered
 * as most appropriate. If necessary, this task also takes care of suspending possibly existing former deployments
 * of the {@link DynamicDeployment} by undeploying the corresponding {@link Operator} from its former target device.
 * The decision for the target device and whether a (re-)deployment is necessary is done according to the following
 * procedure:
 * <ul>
 *     <li>All devices of the device candidates ranking are considered in ascending order of their rank.</li>
 *     <li>If the dynamic deployment is currently already deployed to a device and the currently considered device
 *     has a score that is lower than or equal to the score of this former device in the new ranking, the former
 *     device is continued to be used.</li>
 *     <li>Otherwise, it is tried to deploy the operator to the current device. If the deployment fails, the next
 *     device in the ranking is considered.</li>
 * </ul>
 * <p>
 * This way, it is ensured that re-deployments of already deployed {@link DynamicDeployment} are only executed
 * in case a device is available that appears to be really better suited than the currently used device.
 */
public class DeployByRankingTask implements DynamicDeploymentTask {

    //The original version of the dynamic deployment that is supposed to be deployed
    private DynamicDeployment originalDynamicDeployment;

    /*
    Injected fields
     */
    private final CandidateDevicesProcessor candidateDevicesProcessor;
    private final DiscoveryDeploymentService discoveryDeploymentService;
    private final DynamicDeploymentRepository dynamicDeploymentRepository;
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link DeployByRankingTask} from a given {@link DynamicDeployment}.
     *
     * @param dynamicDeployment The dynamic deployment to use
     */
    public DeployByRankingTask(DynamicDeployment dynamicDeployment) {
        //Set fields
        setDynamicDeployment(dynamicDeployment);

        //Inject components
        this.candidateDevicesProcessor = DynamicBeanProvider.get(CandidateDevicesProcessor.class);
        this.discoveryDeploymentService = DynamicBeanProvider.get(DiscoveryDeploymentService.class);
        this.dynamicDeploymentRepository = DynamicBeanProvider.get(DynamicDeploymentRepository.class);
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read dynamic deployment and candidate devices from their repositories
        Optional<DynamicDeployment> dynamicDeploymentOptional = dynamicDeploymentRepository.findById(this.originalDynamicDeployment.getId());
        Optional<CandidateDevicesResult> candidateDevicesOptional = candidateDevicesRepository.findById(getDeviceTemplateId());

        //Sanity checks
        if ((!dynamicDeploymentOptional.isPresent()) || (!candidateDevicesOptional.isPresent())) {
            //Task ends because data is not available
            return;
        }

        //Get objects from optionals
        DynamicDeployment dynamicDeployment = dynamicDeploymentOptional.get();
        CandidateDevicesResult candidateDevices = candidateDevicesOptional.get();

        //Check intention for dynamic deployment
        if (!dynamicDeployment.isActivatingIntended()) {
            //Active is not intended, so no need to deploy; potential undeployment will be done by another task
            return;
        }

        //Update the state of the dynamic deployment
        this.updateDynamicDeployment(dynamicDeployment.setState(DynamicDeploymentState.IN_PROGRESS));

        //Determine whether the dynamic deployment is currently deployed
        boolean isDeployed = (dynamicDeployment.getLastDeviceDetails() != null) &&
                this.discoveryDeploymentService.isDeployed(dynamicDeployment);

        //Calculate and candidate devices ranking
        CandidateDevicesRanking ranking = this.candidateDevicesProcessor.process(candidateDevices, dynamicDeployment.getDeviceTemplate());

        //Check if ranking is valid
        if ((ranking == null) || (ranking.isEmpty())) {
            //No suitable candidate devices; if there is still an active deployment, we need to undeploy it
            if (isDeployed) {
                this.discoveryDeploymentService.undeploy(dynamicDeployment);
            }
            //Update last device details and status
            updateDynamicDeployment(dynamicDeployment.setLastDeviceDetails(null)
                    .setState(DynamicDeploymentState.NO_CANDIDATE));
            return;
        }

        //Get MAC address of current deployment (if existing)
        String oldMacAddress = dynamicDeployment.getLastDeviceDetails() == null ? null : dynamicDeployment.getLastDeviceDetails().getMacAddress();

        //Determine score of old device (if existing) in the new ranking
        double minScoreExclusive = isDeployed ? ranking.getScoreByMacAddress(oldMacAddress) : -1;

        //Iterate through the ranking
        for (ScoredCandidateDevice candidateDevice : ranking) {
            //Check if score is still in range
            if (candidateDevice.getScore() <= minScoreExclusive) {
                //Deployment is already deployed and the device is still best suited
                updateDynamicDeployment(dynamicDeployment.setState(DynamicDeploymentState.DEPLOYED));
                return;
            }

            //Check if current candidate is the same as the existing deployment (probably this check is not needed)
            if (isDeployed && (candidateDevice.getIdentifiers().getMacAddress().equals(oldMacAddress)))
                continue; //Skip candidate device because it is equal to the current device

            //Try to deploy to candidate device and check for success
            if (this.discoveryDeploymentService.deploy(dynamicDeployment, candidateDevice)) {
                //Success; if former deployment existed, undeploy from former device
                if (isDeployed) this.discoveryDeploymentService.undeploy(dynamicDeployment);

                //Update the dynamic deployment accordingly and set the state
                this.updateDynamicDeployment(dynamicDeployment
                        .setLastDeviceDetails(new DynamicDeploymentDeviceDetails(candidateDevice))
                        .setState(DynamicDeploymentState.DEPLOYED));
                return;
            }
        }

        /*
        All valid candidate devices of the ranking were checked, two cases remain:
        - Case 1: No former deployment existed and deployment failed for all candidate devices of the non-empty ranking
        - Case 2: Former deployment existed and is still most appropriate, because all other candidate devices of
                  the non-empty ranking failed
         */

        //Check if deployment exists
        if (isDeployed) {
            //Former deployment is still the most appropriate deployment, update status accordingly
            updateDynamicDeployment(dynamicDeployment.setState(DynamicDeploymentState.DEPLOYED));
            return;
        }

        //No deployment exists and deployment failed for all candidate devices
        updateDynamicDeployment(dynamicDeployment.setLastDeviceDetails(null)
                .setState(DynamicDeploymentState.ALL_FAILED));
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
        return "[Deploy by ranking]";
    }
}
