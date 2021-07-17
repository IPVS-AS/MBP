package de.ipvs.as.mbp.service.discovery.engine.tasks.dynamic;

import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDeviceDetails;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralStatus;
import de.ipvs.as.mbp.service.discovery.deployment.DeploymentResult;

public class DeployByRankingTask implements DynamicPeripheralTask {

    public DeployByRankingTask() {

    }

    /**
     * Implements the actual operations of the task. It is recommended to check for Thread interruptions in order to
     * gracefully deal with cancellations of the task.
     */
    @Override
    public void run() {

        DynamicPeripheral dynamicPeripheral;
        //TODO read from repo per ID (must be fresh)

        //TODO read candidate devices from repo and calculate ranking using the processor (all data must be fresh)
/*
        //Determine whether the dynamic peripheral is currently deployed
        boolean wasDeployed = (dynamicPeripheral.getLastDeviceDetails() != null) && isDynamicPeripheralDeployed(dynamicPeripheral);

        //Check for empty ranking
        if (ranking.isEmpty()) {
            return (wasDeployed ? DeploymentResult.DEPLOYED : DeploymentResult.EMPTY_RANKING);
        }

        //Remember candidate device for which the deployment was successful
        ScoredCandidateDevice successDevice = null;

        //Get MAC address of current deployment (if existing)
        String oldMacAddress = dynamicPeripheral.getLastDeviceDetails() == null ? null : dynamicPeripheral.getLastDeviceDetails().getMacAddress();

        //Iterate through the ranking
        for (ScoredCandidateDevice candidateDevice : ranking) {
            //Check if score is in range
            if (candidateDevice.getScore() <= minScoreExclusive)
                return (wasDeployed ? DeploymentResult.DEPLOYED : DeploymentResult.ALL_FAILED);

            //Check if current candidate is the same as the existing deployment
            if (wasDeployed && (candidateDevice.getIdentifiers().getMacAddress().equals(oldMacAddress)))
                continue; //Skip candidate device because it is the same again

            //Try to deploy to candidate device and check for success
            if (deployDynamicPeripheral(dynamicPeripheral, candidateDevice)) {
                //Success, remember device
                successDevice = candidateDevice;
                break;
            }
        }

        //Ranking is done, check if deployment was possible
        if (successDevice == null) {
            //No success
            return (wasDeployed ? DeploymentResult.DEPLOYED : DeploymentResult.ALL_FAILED);
        }

        //Undeploy from old device if deployed
        if (wasDeployed) {
            undeployDynamicPeripheral(dynamicPeripheral);
        }

        //Update last device details of the dynamic peripheral
        dynamicPeripheral.setLastDeviceDetails(new DynamicPeripheralDeviceDetails(successDevice));

        //TODO store deployment data in peripheral object and write it to repo

        return DeploymentResult.DEPLOYED;*/
    }

    /**
     * Returns the final {@link DynamicPeripheralStatus} in which the pertaining {@link DynamicPeripheral}
     * is after the completion of this task.
     *
     * @return The result state
     */
    @Override
    public DynamicPeripheralStatus getResultState() {
        return null;
    }

    /**
     * Returns the ID of the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The dynamic peripheral ID
     */
    @Override
    public String getDynamicPeripheralId() {
        return null;
    }


    /**
     * Returns the ID of the device template that is used by the {@link DynamicPeripheral} on which this task operates.
     *
     * @return The device template ID
     */
    @Override
    public String getDeviceTemplateId() {
        return null;
    }
}
