package de.ipvs.as.mbp.service.discovery.deployment;

import de.ipvs.as.mbp.domain.discovery.collections.CandidateDevicesRanking;
import de.ipvs.as.mbp.domain.discovery.collections.ScoredCandidateDevice;
import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDeviceDetails;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class DiscoveryDeploymentExecutor {

    //Number of threads to use in the thread pool that executes deployment tasks
    private static final int THREAD_POOL_SIZE = 5;

    @Autowired
    private DeployerDispatcher deployerDispatcher;

    //The deployer to use, retrieved from the dispatcher
    private IDeployer deployer;

    //Executor service for executing deployment tasks asynchronously
    private ExecutorService executorService;

    /**
     * Creates the deployment executor.
     */
    public DiscoveryDeploymentExecutor() {
        //Create executor service
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    }

    /**
     * Initializes the deployment executor.
     */
    @PostConstruct
    private void init() {
        //Get deployer from the dispatcher
        this.deployer = deployerDispatcher.getDeployer();
    }

    public CompletableFuture<DeploymentResult> deployByRanking(DynamicPeripheral dynamicPeripheral, CandidateDevicesRanking ranking, DeploymentCompletionListener listener) {
        return deployByRanking(dynamicPeripheral, ranking, -1, listener);
    }

    public CompletableFuture<DeploymentResult> deployByRanking(DynamicPeripheral dynamicPeripheral, CandidateDevicesRanking ranking, double minScoreExclusive, DeploymentCompletionListener listener) {
        CompletableFuture<DeploymentResult> completableFuture = CompletableFuture.supplyAsync(() -> {
            return this.deployByRanking(dynamicPeripheral, ranking, minScoreExclusive);
        }, this.executorService);

        //Add callback to the future in order to notify the listener about the result
        completableFuture.thenAccept(deploymentResult -> listener.onDeploymentCompleted(dynamicPeripheral, deploymentResult));

        return completableFuture;
    }

    private DeploymentResult deployByRanking(DynamicPeripheral dynamicPeripheral, CandidateDevicesRanking ranking, double minScoreExclusive) {
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

        return DeploymentResult.DEPLOYED;
    }

    private boolean deployDynamicPeripheral(DynamicPeripheral dynamicPeripheral, DeviceDescription candidateDevice) {
        //Create deployable component
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicPeripheral, candidateDevice);

        try {
            //Deploy component
            deployer.deployComponent(component);

            //Check for success
            //if (!deployer.isComponentDeployed(component)) return false;

            //Start component
            deployer.startComponent(component, Collections.emptyList());

            //Check for success
            return deployer.isComponentRunning(component);
        } catch (Exception e) {
            return false;
        }
    }

    private void undeployDynamicPeripheral(DynamicPeripheral dynamicPeripheral) {
        //Create deployable component
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicPeripheral);

        //Check whether the component is deployed
        try {
            deployer.undeployComponent(component);
        } catch (Exception ignored) {
        }
    }

    /**
     * Checks and returns whether a given {@link DynamicPeripheral} is currently deployed on a device.
     *
     * @param dynamicPeripheral The dynamic peripheral to check
     * @return True, if the dynamic peripheral is deployed; false otherwise
     */
    private boolean isDynamicPeripheralDeployed(DynamicPeripheral dynamicPeripheral) {
        //Create deployable component
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicPeripheral);

        //Check whether the component is deployed
        try {
            return deployer.isComponentDeployed(component);
        } catch (Exception e) {
            return false;
        }
    }
}
