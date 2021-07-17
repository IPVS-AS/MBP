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
    @Autowired
    private DeployerDispatcher deployerDispatcher;

    //The deployer to use, retrieved from the dispatcher
    private IDeployer deployer;

    /**
     * Creates the deployment executor.
     */
    public DiscoveryDeploymentExecutor() {

    }

    /**
     * Initializes the deployment executor.
     */
    @PostConstruct
    private void init() {
        //Get deployer from the dispatcher
        this.deployer = deployerDispatcher.getDeployer();
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
