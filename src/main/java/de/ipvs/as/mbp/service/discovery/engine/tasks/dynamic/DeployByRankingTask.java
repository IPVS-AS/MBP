package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.DynamicBeanProvider;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesResult;
import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;
import de.ipvs.as.mbp.domain.discovery.device.DeviceTemplate;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralState;
import de.ipvs.as.mbp.domain.operator.Operator;
import de.ipvs.as.mbp.repository.discovery.CandidateDevicesRepository;
import de.ipvs.as.mbp.repository.discovery.DynamicPeripheralRepository;
import de.ipvs.as.mbp.service.discovery.deployment.DiscoveryDeploymentService;
import de.ipvs.as.mbp.service.discovery.processing.CandidateDevicesProcessor;

import java.util.Optional;

/**
 * The purpose of this task is to deploy a given {@link DynamicPeripheral} with respect to the ranking which
 * can be computed from the candidate devices in the {@link CandidateDevicesResult} that is associated
 * with the {@link DeviceTemplate} underlying the {@link DynamicPeripheral}. Thereby, the operator of the
 * {@link DynamicPeripheral} is supposed to be deployed to the target device of the ranking that can be considered
 * as most appropriate. If necessary, this task also takes care of suspending possibly existing former deployments
 * of the {@link DynamicPeripheral} by undeploying the corresponding {@link Operator} from its former target device.
 * The decision for the target device and whether a (re-)deployment is necessary is done according to the following
 * procedure:
 * <ul>
 *     <li>All devices of the device candidates ranking are considered in ascending order of their rank.</li>
 *     <li>If the dynamic peripheral is currently already deployed to a device and the currently considered device
 *     has a score that is lower than or equal to the score of this former device in the new ranking, the former
 *     device is continued to be used.</li>
 *     <li>Otherwise, it is tried to deploy the operator to the current device. If the deployment fails, the next
 *     device in the ranking is considered.</li>
 * </ul>
 * <p>
 * This way, it is ensured that re-deployments of already deployed {@link DynamicPeripheral} are only executed
 * in case a device is available that appears to be really better suited than the currently used device.
 */
public class DeployByRankingTask implements DynamicPeripheralTask {

    //The original version of the dynamic peripheral that is supposed to be deployed
    private DynamicPeripheral originalDynamicPeripheral;

    /*
    Injected fields
     */
    private final CandidateDevicesProcessor candidateDevicesProcessor;
    private final DiscoveryDeploymentService discoveryDeploymentService;
    private final DynamicPeripheralRepository dynamicPeripheralRepository;
    private final CandidateDevicesRepository candidateDevicesRepository;

    /**
     * Creates a new {@link DeployByRankingTask} from a given {@link DynamicPeripheral}.
     *
     * @param dynamicPeripheral The dynamic peripheral to use
     */
    public DeployByRankingTask(DynamicPeripheral dynamicPeripheral) {
        //Set fields
        setDynamicPeripheral(dynamicPeripheral);

        //Inject components
        this.candidateDevicesProcessor = DynamicBeanProvider.get(CandidateDevicesProcessor.class);
        this.discoveryDeploymentService = DynamicBeanProvider.get(DiscoveryDeploymentService.class);
        this.dynamicPeripheralRepository = DynamicBeanProvider.get(DynamicPeripheralRepository.class);
        this.candidateDevicesRepository = DynamicBeanProvider.get(CandidateDevicesRepository.class);
    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {
        //Read dynamic peripheral and candidate devices from their repositories
        Optional<DynamicPeripheral> dynamicPeripheralOptional = dynamicPeripheralRepository.findById(this.originalDynamicPeripheral.getId());
        Optional<CandidateDevicesResult> candidateDevicesOptional = candidateDevicesRepository.findById(getDeviceTemplateId());

        //Sanity checks
        if ((!dynamicPeripheralOptional.isPresent()) || (!candidateDevicesOptional.isPresent())) {
            //Task failed because data is not available
            return;
        }

        //Get objects from optionals
        DynamicPeripheral dynamicPeripheral = dynamicPeripheralOptional.get();
        CandidateDevicesResult candidateDevices = candidateDevicesOptional.get();

        //Check intention for dynamic peripheral
        if (!dynamicPeripheral.isActiveIntended()) {
            //Active is not intended, so no need to deploy; potential undeployment will be done by another task
            return;
        }

        //Update the state of the dynamic peripheral
        this.updateDynamicPeripheral(dynamicPeripheral.setState(DynamicPeripheralState.IN_PROGRESS));

        //Determine whether the dynamic peripheral is currently deployed
        boolean isDeployed = (dynamicPeripheral.getLastDeviceDetails() != null) &&
                this.discoveryDeploymentService.isDeployed(dynamicPeripheral);

        //Calculate and candidate devices ranking
        CandidateDevicesRanking ranking = this.candidateDevicesProcessor.process(candidateDevices, dynamicPeripheral.getDeviceTemplate());

        //Check if ranking is valid
        if ((ranking == null) || (ranking.isEmpty())) {
            //No suitable candidate devices; if there is still an active deployment, we need to undeploy it
            if (isDeployed) {
                this.discoveryDeploymentService.undeploy(dynamicPeripheral);
            }
            //Update last device details and status
            updateDynamicPeripheral(dynamicPeripheral.setLastDeviceDetails(null)
                    .setState(DynamicPeripheralState.NO_CANDIDATE));
            return;
        }

        //Get MAC address of current deployment (if existing)
        String oldMacAddress = dynamicPeripheral.getLastDeviceDetails() == null ? null : dynamicPeripheral.getLastDeviceDetails().getMacAddress();

        //Determine score of old device (if existing) in the new ranking
        double minScoreExclusive = isDeployed ? ranking.getScoreByMacAddress(oldMacAddress) : -1;

        //Iterate through the ranking
        for (ScoredCandidateDevice candidateDevice : ranking) {
            //Check if score is still in range
            if (candidateDevice.getScore() <= minScoreExclusive) {
                //Peripheral is already deployed and the device is still best suited
                updateDynamicPeripheral(dynamicPeripheral.setState(DynamicPeripheralState.DEPLOYED));
                return;
            }

            //Check if current candidate is the same as the existing deployment (probably this check is not needed)
            if (isDeployed && (candidateDevice.getIdentifiers().getMacAddress().equals(oldMacAddress)))
                continue; //Skip candidate device because it is equal to the current device

            //Try to deploy to candidate device and check for success
            if (this.discoveryDeploymentService.deploy(dynamicPeripheral, candidateDevice)) {
                //Success; if former deployment existed, undeploy from former device
                if (isDeployed) this.discoveryDeploymentService.undeploy(dynamicPeripheral);

                //Update the dynamic peripheral accordingly and set the state
                this.updateDynamicPeripheral(dynamicPeripheral
                        .setLastDeviceDetails(new DynamicPeripheralDeviceDetails(candidateDevice))
                        .setState(DynamicPeripheralState.DEPLOYED));
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
            updateDynamicPeripheral(dynamicPeripheral.setState(DynamicPeripheralState.DEPLOYED));
            return;
        }

        //No deployment exists and deployment failed for all candidate devices
        updateDynamicPeripheral(dynamicPeripheral.setLastDeviceDetails(null)
                .setState(DynamicPeripheralState.ALL_FAILED));
    }

    /**
     * Writes a given {@link DynamicPeripheral} to the repository, thus updating its fields in the database.
     *
     * @param dynamicPeripheral The dynamic peripheral to update
     */
    private void updateDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Write state to repository
        this.dynamicPeripheralRepository.save(dynamicPeripheral);
    }

    /**
     * Returns the {@link DynamicPeripheral} object that has been originally passed to this task and is
     * supposed to be (re-)deployed.
     *
     * @return The dynamic peripheral
     */
    public DynamicPeripheral getDynamicPeripheral() {
        return this.originalDynamicPeripheral;
    }

    /**
     * Sets the {@link DynamicPeripheral} that is supposed to be (re-)deployed within this task.
     *
     * @param dynamicPeripheral The dynamic peripheral to set
     */
    private void setDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Null check
        if (dynamicPeripheral == null) {
            throw new IllegalArgumentException("The dynamic peripheral must not be null.");
        }

        this.originalDynamicPeripheral = dynamicPeripheral;
    }

    /**
     * Returns the final {@link DynamicPeripheralState} in which the pertaining {@link DynamicPeripheral}
     * is after the completion of this task.
     *
     * @return The result state
     */
    @Override
    public DynamicPeripheralState getResultState() {
        return null;
    }

    /**
     * Returns the ID of the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The dynamic peripheral ID
     */
    @Override
    public String getDynamicPeripheralId() {
        return this.originalDynamicPeripheral.getId();
    }


    /**
     * Returns the ID of the device template that is used by the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The device template ID
     */
    @Override
    public String getDeviceTemplateId() {
        return this.originalDynamicPeripheral.getDeviceTemplate().getId();
    }
}
