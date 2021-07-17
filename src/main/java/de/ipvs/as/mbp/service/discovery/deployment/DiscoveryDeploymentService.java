package de.ipvs.as.mbp.service.discovery.deployment;

import de.ipvs.as.mbp.domain.discovery.description.DeviceDescription;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheral;
import de.ipvs.as.mbp.domain.discovery.peripheral.DynamicPeripheralDeviceDetails;
import de.ipvs.as.mbp.service.deployment.DeployerDispatcher;
import de.ipvs.as.mbp.service.deployment.IDeployer;
import de.ipvs.as.mbp.service.discovery.engine.DiscoveryEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;

/**
 * Offers services related to the deployment of {@link DynamicPeripheral} in the scope of device discovery operations
 * within the {@link DiscoveryEngine}.
 */
@Service
public class DiscoveryDeploymentService {
    //Dispatcher from which the deployer can be retrieved
    @Autowired
    private DeployerDispatcher deployerDispatcher;

    /**
     * Creates the deployment executor.
     */
    public DiscoveryDeploymentService() {

    }

    /**
     * Initializes the deployment executor.
     */
    @PostConstruct
    private void init() {

    }

    /**
     * Tries to deploy a given {@link DynamicPeripheral} to a device, given by its {@link DeviceDescription} and to
     * start the associated operator on this device. The {@link Boolean} that is returned as result of this method
     * indicates whether the deployment and the starting of the operator were successful.
     *
     * @param dynamicPeripheral The dynamic peripheral to deploy
     * @param device            The description of the device on which the dynamic peripheral is supposed to be deployed
     * @return True, if the deployment and starting of the operator were successful; false otherwise
     */
    public boolean deploy(DynamicPeripheral dynamicPeripheral, DeviceDescription device) {
        //Create deployable component
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicPeripheral, device);

        //Get deployer from dispatcher
        IDeployer deployer = this.deployerDispatcher.getDeployer();

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

    /**
     * Undeploys a given {@link DynamicPeripheral} from the device that is referenced as
     * {@link DynamicPeripheralDeviceDetails} within the {@link DynamicPeripheral} in case it is currently deployed.
     *
     * @param dynamicPeripheral The dynamic peripheral to undeploy
     */
    public void undeploy(DynamicPeripheral dynamicPeripheral) {
        //Create deployable component
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicPeripheral);

        //Get deployer from the dispatcher
        IDeployer deployer = this.deployerDispatcher.getDeployer();

        try {
            //Undeploy
            deployer.undeployComponent(component);
        } catch (Exception ignored) {
        }
    }

    /**
     * Checks and returns whether a given {@link DynamicPeripheral} is currently deployed to the device that is
     * referenced as {@link DynamicPeripheralDeviceDetails} within the {@link DynamicPeripheral}.
     *
     * @param dynamicPeripheral The dynamic peripheral to check
     * @return True, if the dynamic peripheral is deployed; false otherwise
     */
    public boolean isDeployed(DynamicPeripheral dynamicPeripheral) {
        //Create deployable component
        DynamicDeployableComponent component = new DynamicDeployableComponent(dynamicPeripheral);

        //Get deployer from the dispatcher
        IDeployer deployer = this.deployerDispatcher.getDeployer();

        //Check whether the component is deployed
        try {
            return deployer.isComponentDeployed(component);
        } catch (Exception e) {
            return false;
        }
    }
}
